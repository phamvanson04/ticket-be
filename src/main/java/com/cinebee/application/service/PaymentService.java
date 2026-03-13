package com.cinebee.application.service;

import com.cinebee.presentation.dto.request.MoMoPaymentRequest;
import com.cinebee.presentation.dto.response.MoMoPaymentResponse;

import java.util.Map;

public interface PaymentService {
    MoMoPaymentResponse createMomoPayment(MoMoPaymentRequest request);
    void handleMomoIpn(Map<String, Object> momoIpnPayload);
    void handleMomoReturn(String orderId, String resultCode, String message);
    boolean verifyMomoReturnSignature(Map<String, String> params);

}

