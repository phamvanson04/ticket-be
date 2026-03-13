package com.cinebee.application.service.impl;

import com.cinebee.presentation.dto.request.MoMoPaymentRequest;
import com.cinebee.presentation.dto.response.MoMoPaymentResponse;
import com.cinebee.domain.entity.Payment;
import com.cinebee.domain.entity.Ticket;
import com.cinebee.domain.entity.User;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.infrastructure.persistence.repository.PaymentRepository;
import com.cinebee.infrastructure.persistence.repository.TicketRepository;
import com.cinebee.infrastructure.persistence.repository.UserRepository;
import com.cinebee.application.service.PaymentService;
import com.cinebee.application.service.payment.PaymentGatewayStrategy;
import com.cinebee.application.service.TicketConfirmationEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final String MOMO_PROVIDER = "MOMO";

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketConfirmationEmailService ticketConfirmationEmailService;
    private final Map<String, PaymentGatewayStrategy> strategyByProvider;

    public PaymentServiceImpl(
        PaymentRepository paymentRepository,
        TicketRepository ticketRepository,
        UserRepository userRepository,
        TicketConfirmationEmailService ticketConfirmationEmailService,
        List<PaymentGatewayStrategy> paymentGatewayStrategies
    ) {
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketConfirmationEmailService = ticketConfirmationEmailService;
        this.strategyByProvider = paymentGatewayStrategies.stream()
            .collect(Collectors.toMap(PaymentGatewayStrategy::providerCode, Function.identity()));
    }

    @Override
    @Transactional
    public MoMoPaymentResponse createMomoPayment(MoMoPaymentRequest request) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Creating MoMo payment for user: {}, ticketId: {}", authenticatedUsername, request.getTicketId());

        User authenticatedUser = userRepository.findByUsername(authenticatedUsername)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + authenticatedUsername));
        log.info("Found user: {}, ID: {}", authenticatedUser.getUsername(), authenticatedUser.getId());

        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ApiException(ErrorCode.TICKET_NOT_FOUND));
        log.info("Found ticket: ID={}, User={}, Price={}", ticket.getId(),
                ticket.getUser() != null ? ticket.getUser().getUsername() : "null", ticket.getPrice());

        boolean isAdmin = authenticatedUser.getRole() == com.cinebee.shared.common.Role.ADMIN;
        if (!ticket.getUser().getId().equals(authenticatedUser.getId()) && !isAdmin) {
            log.error("Unauthorized access: User {} trying to pay for ticket belonging to user {}",
                    authenticatedUser.getUsername(), ticket.getUser().getUsername());
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You are not authorized to pay for this ticket.");
        }

        if (isAdmin) {
            log.info("Admin user {} is paying for ticket of user {}",
                    authenticatedUser.getUsername(), ticket.getUser().getUsername());
        }

        if (paymentRepository.existsByTicketAndPaymentStatus(ticket, Payment.PaymentStatus.COMPLETED)) {
            log.error("Payment already completed for ticket ID: {}", ticket.getId());
            throw new ApiException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        log.info("Payment details: orderId={}, requestId={}, amount={}", orderId, requestId, ticket.getPrice().longValue());

        Payment payment = new Payment(ticket, authenticatedUser, orderId, requestId);
        paymentRepository.save(payment);
        log.info("Saved payment record with ID: {}", payment.getId());

        try {
            return resolveStrategy(MOMO_PROVIDER).createPayment(payment, ticket);
        } catch (Exception e) {
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("Exception during MoMo payment creation", e);
            throw new ApiException(ErrorCode.PAYMENT_CREATION_FAILED, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleMomoIpn(Map<String, Object> momoIpnPayload) {
        log.info("Received MoMo IPN: {}", momoIpnPayload);

        String orderId = (String) momoIpnPayload.get("orderId");
        String amount = momoIpnPayload.get("amount").toString();
        String resultCode = momoIpnPayload.get("resultCode").toString();
        String message = (String) momoIpnPayload.get("message");

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("IPN Error: Payment with orderId {} not found.", orderId);
                    return new RuntimeException("Payment not found");
                });

        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED || payment.getPaymentStatus() == Payment.PaymentStatus.FAILED) {
            log.warn("IPN Info: Payment with orderId {} has already been processed. Status: {}", orderId, payment.getPaymentStatus());
            return;
        }

        if (payment.getAmount().longValue() != Long.parseLong(amount)) {
            log.error("IPN VALIDATION FAILED for orderId {}: Amount mismatch. Expected: {}, Received: {}",
                    orderId, payment.getAmount().longValue(), amount);
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return;
        }

        if (!resolveStrategy(MOMO_PROVIDER).verifyIpnSignature(momoIpnPayload)) {
            log.error("IPN VALIDATION FAILED for orderId {}: Signature mismatch.", orderId);
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return;
        }

        if (resolveStrategy(MOMO_PROVIDER).isSuccessResult(resultCode)) {
            log.info("IPN SUCCESS: Payment for orderId {} is completed.", orderId);
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);

            try {
                Ticket ticket = payment.getTicket();
                ticketConfirmationEmailService.sendTicketConfirmationEmail(ticket);
                log.info("Ticket confirmation email sent for ticket {}", ticket.getId());
            } catch (Exception emailError) {
                log.error("Failed to send confirmation email for ticket {}: {}",
                    payment.getTicket().getId(), emailError.getMessage());
            }
        } else {
            log.warn("IPN FAILED: Payment for orderId {} failed with message: {}", orderId, message);
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        }
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void handleMomoReturn(String orderId, String resultCode, String message) {
        log.info("Processing MoMo return: orderId={}, resultCode={}, message={}", orderId, resultCode, message);
        
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Return Error: Payment with orderId {} not found.", orderId);
                    return new RuntimeException("Payment not found");
                });

        if (payment.getPaymentStatus() == Payment.PaymentStatus.PENDING) {
            if (resolveStrategy(MOMO_PROVIDER).isSuccessResult(resultCode)) {
                log.info("Return SUCCESS: Payment for orderId {} is completed.", orderId);
                payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);

                try {
                    Ticket ticket = payment.getTicket();
                    ticketConfirmationEmailService.sendTicketConfirmationEmail(ticket);
                    log.info("Ticket confirmation email sent for ticket {} via return callback", ticket.getId());
                } catch (Exception emailError) {
                    log.error("Failed to send confirmation email for ticket {} via return callback: {}",
                        payment.getTicket().getId(), emailError.getMessage());
                }
            } else {
                log.warn("Return FAILED: Payment for orderId {} failed with message: {}", orderId, message);
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);
            log.info("Updated payment status to: {}", payment.getPaymentStatus());
        } else {
            log.info("Payment {} already processed with status: {}", orderId, payment.getPaymentStatus());
        }
    }

    @Override
    public boolean verifyMomoReturnSignature(Map<String, String> params) {
        return resolveStrategy(MOMO_PROVIDER).verifyReturnSignature(params);
    }

    private PaymentGatewayStrategy resolveStrategy(String providerCode) {
        PaymentGatewayStrategy strategy = strategyByProvider.get(providerCode);
        if (strategy == null) {
            throw new ApiException(ErrorCode.PAYMENT_CREATION_FAILED, "Unsupported payment provider: " + providerCode);
        }
        return strategy;
    }
}

