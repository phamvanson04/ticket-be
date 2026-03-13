package com.cinebee.application.service.payment;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.cinebee.domain.entity.Payment;
import com.cinebee.domain.entity.Ticket;
import com.cinebee.infrastructure.config.MoMoConfig;
import com.cinebee.presentation.dto.response.MoMoPaymentResponse;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.shared.util.MoMoSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MoMoPaymentGatewayStrategy implements PaymentGatewayStrategy {

    private static final String PROVIDER_CODE = "MOMO";

    private final MoMoConfig momoConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public MoMoPaymentResponse createPayment(Payment payment, Ticket ticket) {
        String orderId = payment.getOrderId();
        String requestId = payment.getRequestId();
        long amount = ticket.getPrice().longValue();
        String orderInfo = "Thanh toan ve xem phim cho " + ticket.getShowtime().getMovie().getTitle();

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
            String signature = MoMoSecurityUtils.generateSignature(rawSignature, momoConfig.getSecretKey());
            momoRequestPayload.put("signature", signature);

            String requestBodyJson = objectMapper.writeValueAsString(momoRequestPayload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpRequestEntity = new HttpEntity<>(requestBodyJson, headers);

            Map<String, Object> momoApiResponse = restTemplate.postForObject(momoConfig.getEndpoint(), httpRequestEntity, Map.class);
            log.info("MoMo response payload: {}", momoApiResponse);

            if (momoApiResponse != null && "0".equals(String.valueOf(momoApiResponse.get("errorCode")))) {
                return new MoMoPaymentResponse((String) momoApiResponse.get("payUrl"));
            }

            throw new ApiException(ErrorCode.PAYMENT_CREATION_FAILED);
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("MoMo payment creation failed", exception);
            throw new ApiException(ErrorCode.PAYMENT_CREATION_FAILED, exception.getMessage());
        }
    }

    @Override
    public boolean verifyIpnSignature(Map<String, Object> ipnPayload) {
        String requestId = String.valueOf(ipnPayload.get("requestId"));
        String amount = String.valueOf(ipnPayload.get("amount"));
        String orderId = String.valueOf(ipnPayload.get("orderId"));
        String resultCode = String.valueOf(ipnPayload.get("resultCode"));
        String message = String.valueOf(ipnPayload.get("message"));
        String receivedSignature = String.valueOf(ipnPayload.get("signature"));

        String rawSignature = "partnerCode=" + momoConfig.getPartnerCode() +
            "&accessKey=" + momoConfig.getAccessKey() +
            "&requestId=" + requestId +
            "&amount=" + amount +
            "&orderId=" + orderId +
            "&orderInfo=" + ipnPayload.get("orderInfo") +
            "&orderType=" + ipnPayload.get("orderType") +
            "&transId=" + ipnPayload.get("transId") +
            "&resultCode=" + resultCode +
            "&message=" + message +
            "&payType=" + ipnPayload.get("payType") +
            "&responseTime=" + ipnPayload.get("responseTime") +
            "&extraData=" + ipnPayload.get("extraData");

        try {
            String expectedSignature = MoMoSecurityUtils.generateSignature(rawSignature, momoConfig.getSecretKey());
            return expectedSignature.equals(receivedSignature);
        } catch (Exception exception) {
            log.error("Cannot verify MoMo IPN signature", exception);
            return false;
        }
    }

    @Override
    public boolean verifyReturnSignature(Map<String, String> params) {
        try {
            String rawSignature = "partnerCode=" + params.get("partnerCode") +
                "&accessKey=" + params.get("accessKey") +
                "&requestId=" + params.get("requestId") +
                "&amount=" + params.get("amount") +
                "&orderId=" + params.get("orderId") +
                "&orderInfo=" + params.get("orderInfo") +
                "&orderType=" + params.get("orderType") +
                "&transId=" + params.get("transId") +
                "&message=" + params.get("message") +
                "&localMessage=" + params.get("localMessage") +
                "&responseTime=" + params.get("responseTime") +
                "&errorCode=" + params.get("errorCode") +
                "&payType=" + params.get("payType") +
                "&extraData=" + (params.get("extraData") != null ? params.get("extraData") : "");

            String expectedSignature = MoMoSecurityUtils.generateSignature(rawSignature, momoConfig.getSecretKey());
            return expectedSignature.equals(params.get("signature"));
        } catch (Exception exception) {
            log.error("Cannot verify MoMo return signature", exception);
            return false;
        }
    }

    @Override
    public boolean isSuccessResult(String resultCode) {
        return "0".equals(resultCode);
    }
}
