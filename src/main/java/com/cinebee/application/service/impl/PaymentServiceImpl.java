package com.cinebee.application.service.impl;

import com.cinebee.infrastructure.config.MomoConfig;
import com.cinebee.presentation.dto.request.MomoPaymentRequest;
import com.cinebee.presentation.dto.response.MomoPaymentResponse;
import com.cinebee.domain.entity.Payment;
import com.cinebee.domain.entity.Ticket;
import com.cinebee.domain.entity.User;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.infrastructure.persistence.repository.PaymentRepository;
import com.cinebee.infrastructure.persistence.repository.TicketRepository;
import com.cinebee.infrastructure.persistence.repository.UserRepository;
import com.cinebee.application.service.PaymentService;
import com.cinebee.application.service.TicketEmailService;
import com.cinebee.shared.util.MomoSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketEmailService ticketEmailService;

    @Override
    @Transactional
    public MomoPaymentResponse createMomoPayment(MomoPaymentRequest request) {
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
        long amount = ticket.getPrice().longValue();
        String orderInfo = "Thanh toan ve xem phim cho " + ticket.getShowtime().getMovie().getTitle();

        log.info("Payment details: orderId={}, requestId={}, amount={}", orderId, requestId, amount);

        Payment payment = new Payment(ticket, authenticatedUser, orderId, requestId);
        paymentRepository.save(payment);
        log.info("Saved payment record with ID: {}", payment.getId());

        Map<String, Object> momoRequestPayload = new HashMap<>();
        momoRequestPayload.put("partnerCode", momoConfig.getPartnerCode());
        momoRequestPayload.put("accessKey", momoConfig.getAccessKey());
        momoRequestPayload.put("requestId", requestId);
        momoRequestPayload.put("amount", String.valueOf(amount));
        momoRequestPayload.put("orderId", orderId);
        momoRequestPayload.put("orderInfo", orderInfo);
        momoRequestPayload.put("returnUrl", momoConfig.getRedirectUrl());
        momoRequestPayload.put("notifyUrl", momoConfig.getIpnUrl());
        momoRequestPayload.put("requestType", "captureMoMoWallet");
        momoRequestPayload.put("extraData", "");

        log.info("MoMo Config - PartnerCode: {}, AccessKey: {}, Endpoint: {}",
                momoConfig.getPartnerCode(), momoConfig.getAccessKey(), momoConfig.getEndpoint());

        String rawSignature = "partnerCode=" + momoConfig.getPartnerCode() +
                "&accessKey=" + momoConfig.getAccessKey() +
                "&requestId=" + requestId +
                "&amount=" + amount +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&returnUrl=" + momoConfig.getRedirectUrl() +
                "&notifyUrl=" + momoConfig.getIpnUrl() +
                "&extraData=";

        try {
            log.info("Raw signature string: {}", rawSignature);
            String signature = MomoSecurityUtils.generateSignature(rawSignature, momoConfig.getSecretKey());
            log.info("Generated signature: {}", signature);
            momoRequestPayload.put("signature", signature);

            String requestBodyJson = objectMapper.writeValueAsString(momoRequestPayload);
            log.info("MoMo Request Body: {}", requestBodyJson);
            log.info("MoMo Endpoint: {}", momoConfig.getEndpoint());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpRequestEntity = new HttpEntity<>(requestBodyJson, headers);

            Map<String, Object> momoApiResponse = restTemplate.postForObject(momoConfig.getEndpoint(), httpRequestEntity, Map.class);
            log.info("MoMo Response: {}", momoApiResponse);

            if (momoApiResponse != null && "0".equals(momoApiResponse.get("errorCode").toString())) {
                return new MomoPaymentResponse((String) momoApiResponse.get("payUrl"));
            } else {
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.error("Failed to create MoMo payment. ErrorCode: {}, Message: {}, Full Response: {}",
                        momoApiResponse != null ? momoApiResponse.get("errorCode") : "null",
                        momoApiResponse != null ? momoApiResponse.get("message") : "Unknown error",
                        momoApiResponse);
                throw new ApiException(ErrorCode.PAYMENT_CREATION_FAILED);
            }
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
        String requestId = (String) momoIpnPayload.get("requestId");
        String amount = momoIpnPayload.get("amount").toString();
        String resultCode = momoIpnPayload.get("resultCode").toString();
        String message = (String) momoIpnPayload.get("message");
        String receivedSignature = (String) momoIpnPayload.get("signature");

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

        String rawSignature = "partnerCode=" + momoConfig.getPartnerCode() +
                "&accessKey=" + momoConfig.getAccessKey() +
                "&requestId=" + requestId +
                "&amount=" + amount +
                "&orderId=" + orderId +
            "&orderInfo=" + momoIpnPayload.get("orderInfo") +
            "&orderType=" + momoIpnPayload.get("orderType") +
            "&transId=" + momoIpnPayload.get("transId") +
                "&resultCode=" + resultCode +
                "&message=" + message +
            "&payType=" + momoIpnPayload.get("payType") +
            "&responseTime=" + momoIpnPayload.get("responseTime") +
            "&extraData=" + momoIpnPayload.get("extraData");

        try {
            String expectedSignature = MomoSecurityUtils.generateSignature(rawSignature, momoConfig.getSecretKey());
            if (!expectedSignature.equals(receivedSignature)) {
                log.error("IPN VALIDATION FAILED for orderId {}: Signature mismatch.", orderId);
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
                return;
            }
        } catch (Exception e) {
            log.error("IPN Error: Signature generation failed for orderId {}", orderId, e);
            return;
        }

        if ("0".equals(resultCode)) {
            log.info("IPN SUCCESS: Payment for orderId {} is completed.", orderId);
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);

            try {
                Ticket ticket = payment.getTicket();
                ticketEmailService.sendTicketConfirmationEmail(ticket);
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
            if ("0".equals(resultCode)) {
                log.info("Return SUCCESS: Payment for orderId {} is completed.", orderId);
                payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);

                try {
                    Ticket ticket = payment.getTicket();
                    ticketEmailService.sendTicketConfirmationEmail(ticket);
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
        try {
            String partnerCode = params.get("partnerCode");
            String accessKey = params.get("accessKey");
            String requestId = params.get("requestId");
            String amount = params.get("amount");
            String orderId = params.get("orderId");
            String orderInfo = params.get("orderInfo");
            String orderType = params.get("orderType");
            String transId = params.get("transId");
            String message = params.get("message");
            String localMessage = params.get("localMessage");
            String responseTime = params.get("responseTime");
            String errorCode = params.get("errorCode");
            String payType = params.get("payType");
            String extraData = params.get("extraData");
            String receivedSignature = params.get("signature");

            String rawSignature = "partnerCode=" + partnerCode +
                    "&accessKey=" + accessKey +
                    "&requestId=" + requestId +
                    "&amount=" + amount +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&transId=" + transId +
                    "&message=" + message +
                    "&localMessage=" + localMessage +
                    "&responseTime=" + responseTime +
                    "&errorCode=" + errorCode +
                    "&payType=" + payType +
                    "&extraData=" + (extraData != null ? extraData : "");

            log.info("Return Raw signature string: {}", rawSignature);
            String expectedSignature = MomoSecurityUtils.generateSignature(rawSignature, momoConfig.getSecretKey());
            
            log.info("Return Signature verification - Expected: {}, Received: {}", expectedSignature, receivedSignature);
            return expectedSignature.equals(receivedSignature);
            
        } catch (Exception e) {
            log.error("Error verifying MoMo return signature", e);
            return false;
        }
    }
}

