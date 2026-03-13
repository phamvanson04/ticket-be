package com.cinebee.application.service;

import com.cinebee.presentation.dto.request.MomoPaymentRequest;
import com.cinebee.presentation.dto.response.MomoPaymentResponse;

import java.util.Map;

public interface PaymentService {
    MomoPaymentResponse createMomoPayment(MomoPaymentRequest request);
    void handleMomoIpn(Map<String, Object> momoIpnPayload);
    void handleMomoReturn(String orderId, String resultCode, String message);
    boolean verifyMomoReturnSignature(Map<String, String> params);

}

