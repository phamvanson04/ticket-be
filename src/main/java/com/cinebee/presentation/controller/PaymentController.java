package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.MoMoPaymentRequest;
import com.cinebee.presentation.dto.response.MoMoPaymentResponse;
import com.cinebee.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/momo/create")
    public ResponseEntity<MoMoPaymentResponse> createMomoPayment(@RequestBody MoMoPaymentRequest request) {
        return ResponseEntity.ok(paymentService.createMomoPayment(request));
    }

    @PostMapping("/momo/ipn")
    public ResponseEntity<Void> handleMomoIpn(@RequestBody Map<String, Object> momoIpnPayload) {
        try {
            paymentService.handleMomoIpn(momoIpnPayload);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.error("Error processing MoMo IPN, but returning 204 to stop retries. Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @GetMapping("/momo/return")
    public ResponseEntity<Map<String, Object>> handleMomoReturn(
            @RequestParam Map<String, String> allParams) {
        
        log.info("Received MoMo return with all parameters: {}", allParams);
        
        String orderId = allParams.get("orderId");
        String resultCode = allParams.get("resultCode");
        String errorCode = allParams.get("errorCode");
        String message = allParams.get("message");
        String signature = allParams.get("signature");

        String actualResultCode = resultCode != null ? resultCode : errorCode;

        log.info("Processing return: orderId={}, resultCode={}, errorCode={}", orderId, resultCode, errorCode);

        try {
            if (orderId != null && actualResultCode != null) {
                boolean isValidSignature = paymentService.verifyMomoReturnSignature(allParams);

                if (isValidSignature) {
                    paymentService.handleMomoReturn(orderId, actualResultCode, message);
                    log.info("SECURE: Payment status updated after signature verification");
                } else {
                    log.error("SECURITY WARNING: Invalid signature in MoMo return. Possible fraud attempt!");
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("error", "Invalid signature");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }
            } else {
                log.error("Missing required parameters: orderId={}, resultCode/errorCode={}", orderId, actualResultCode);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("resultCode", actualResultCode);
            response.put("message", "0".equals(actualResultCode) ? "Payment successful" : "Payment failed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing MoMo return", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

