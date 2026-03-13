package com.cinebee.application.service.impl;

import com.cinebee.infrastructure.config.MomoConfig;
import com.cinebee.presentation.dto.request.MomoPaymentRequest;
import com.cinebee.presentation.dto.response.MomoResponse;
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
    public MomoResponse createMomoPayment(MomoPaymentRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Creating MoMo payment for user: {}, ticketId: {}", currentUsername, request.getTicketId());

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));
        log.info("Found user: {}, ID: {}", currentUser.getUsername(), currentUser.getId());

        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ApiException(ErrorCode.TICKET_NOT_FOUND));
        log.info("Found ticket: ID={}, User={}, Price={}", ticket.getId(),
                ticket.getUser() != null ? ticket.getUser().getUsername() : "null", ticket.getPrice());

        // Ensure the ticket belongs to the current user OR current user is ADMIN
        boolean isAdmin = currentUser.getRole() == com.cinebee.shared.common.Role.ADMIN;
        if (!ticket.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            log.error("Unauthorized access: User {} trying to pay for ticket belonging to user {}",
                    currentUser.getUsername(), ticket.getUser().getUsername());
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You are not authorized to pay for this ticket.");
        }

        if (isAdmin) {
            log.info("Admin user {} is paying for ticket of user {}",
                    currentUser.getUsername(), ticket.getUser().getUsername());
        }

        // Check if the ticket has already been paid
        if (paymentRepository.existsByTicketAndPaymentStatus(ticket, Payment.PaymentStatus.COMPLETED)) {
            log.error("Payment already completed for ticket ID: {}", ticket.getId());
            throw new ApiException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        String orderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        long amount = ticket.getPrice().longValue();
        String orderInfo = "Thanh toan ve xem phim cho " + ticket.getShowtime().getMovie().getTitle();

        log.info("Payment details: orderId={}, requestId={}, amount={}", orderId, requestId, amount);

        // Create and save the payment record BEFORE contacting MoMo
        Payment payment = new Payment(ticket, currentUser, orderId, requestId);
        paymentRepository.save(payment);
        log.info("Saved payment record with ID: {}", payment.getId());

        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("partnerCode", momoConfig.getPartnerCode());
        requestBodyMap.put("accessKey", momoConfig.getAccessKey());
        requestBodyMap.put("requestId", requestId);
        requestBodyMap.put("amount", String.valueOf(amount));
        requestBodyMap.put("orderId", orderId);
        requestBodyMap.put("orderInfo", orderInfo);
        requestBodyMap.put("returnUrl", momoConfig.getRedirectUrl());
        requestBodyMap.put("notifyUrl", momoConfig.getIpnUrl());
        requestBodyMap.put("requestType", "captureMoMoWallet");
        requestBodyMap.put("extraData", "");

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
            requestBodyMap.put("signature", signature);

            String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);
            log.info("MoMo Request Body: {}", requestBodyJson);
            log.info("MoMo Endpoint: {}", momoConfig.getEndpoint());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            Map<String, Object> result = restTemplate.postForObject(momoConfig.getEndpoint(), entity, Map.class);
            log.info("MoMo Response: {}", result);

            if (result != null && "0".equals(result.get("errorCode").toString())) {
                return new MomoResponse((String) result.get("payUrl"));
            } else {
                payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.error("Failed to create MoMo payment. ErrorCode: {}, Message: {}, Full Response: {}",
                        result != null ? result.get("errorCode") : "null",
                        result != null ? result.get("message") : "Unknown error",
                        result);
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
    public void handleMomoIpn(Map<String, Object> ipnData) {
        log.info("Received MoMo IPN: {}", ipnData);

        String orderId = (String) ipnData.get("orderId");
        String requestId = (String) ipnData.get("requestId");
        String amount = ipnData.get("amount").toString();
        String resultCode = ipnData.get("resultCode").toString();
        String message = (String) ipnData.get("message");
        String receivedSignature = (String) ipnData.get("signature");

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("IPN Error: Payment with orderId {} not found.", orderId);
                    // We don't throw exception here to prevent MoMo from resending IPN for an invalid order
                    return new RuntimeException("Payment not found");
                });

        // Prevent reprocessing a completed or failed transaction
        if (payment.getPaymentStatus() == Payment.PaymentStatus.COMPLETED || payment.getPaymentStatus() == Payment.PaymentStatus.FAILED) {
            log.warn("IPN Info: Payment with orderId {} has already been processed. Status: {}", orderId, payment.getPaymentStatus());
            return;
        }

        // --- DATA INTEGRITY VALIDATION ---
        // 1. Verify amount
        if (payment.getAmount().longValue() != Long.parseLong(amount)) {
            log.error("IPN VALIDATION FAILED for orderId {}: Amount mismatch. Expected: {}, Received: {}",
                    orderId, payment.getAmount().longValue(), amount);
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return;
        }

        // 2. Verify signature (CRITICAL FOR SECURITY)
        String rawSignature = "partnerCode=" + momoConfig.getPartnerCode() +
                "&accessKey=" + momoConfig.getAccessKey() +
                "&requestId=" + requestId +
                "&amount=" + amount +
                "&orderId=" + orderId +
                "&orderInfo=" + ipnData.get("orderInfo") +
                "&orderType=" + ipnData.get("orderType") +
                "&transId=" + ipnData.get("transId") +
                "&resultCode=" + resultCode +
                "&message=" + message +
                "&payType=" + ipnData.get("payType") +
                "&responseTime=" + ipnData.get("responseTime") +
                "&extraData=" + ipnData.get("extraData");

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

        // --- UPDATE DATABASE ---
        if ("0".equals(resultCode)) {
            log.info("IPN SUCCESS: Payment for orderId {} is completed.", orderId);
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            
            // âœ¨ Gá»¬I EMAIL XÃC NHáº¬N VÃ‰ THÃ€NH CÃ”NG
            try {
                Ticket ticket = payment.getTicket();
                ticketEmailService.sendTicketConfirmationEmail(ticket);
                log.info("âœ… Ticket confirmation email sent for ticket {}", ticket.getId());
            } catch (Exception emailError) {
                log.error("âŒ Failed to send confirmation email for ticket {}: {}", 
                    payment.getTicket().getId(), emailError.getMessage());
                // KhÃ´ng throw error vÃ¬ payment Ä‘Ã£ thÃ nh cÃ´ng
            }
            
            // Here you can trigger other business logic, e.g., finalize the ticket
            // ticket.setStatus(TicketStatus.CONFIRMED);
            // ticketRepository.save(ticket);
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

        // Only update if payment is still pending
        if (payment.getPaymentStatus() == Payment.PaymentStatus.PENDING) {
            if ("0".equals(resultCode)) {
                log.info("Return SUCCESS: Payment for orderId {} is completed.", orderId);
                payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                
                // âœ¨ Gá»¬I EMAIL XÃC NHáº¬N VÃ‰ NGAY KHI RETURN THÃ€NH CÃ”NG
                try {
                    Ticket ticket = payment.getTicket();
                    ticketEmailService.sendTicketConfirmationEmail(ticket);
                    log.info("âœ… Ticket confirmation email sent for ticket {} via Return", ticket.getId());
                } catch (Exception emailError) {
                    log.error("âŒ Failed to send confirmation email for ticket {} via Return: {}", 
                        payment.getTicket().getId(), emailError.getMessage());
                    // KhÃ´ng throw error vÃ¬ payment Ä‘Ã£ thÃ nh cÃ´ng
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

            // Thá»­ thá»© tá»± giá»‘ng nhÆ° MoMo tráº£ vá» trong log
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

