package com.cinebee.application.service.payment;

import java.util.Map;

import com.cinebee.domain.entity.Payment;
import com.cinebee.domain.entity.Ticket;
import com.cinebee.presentation.dto.response.MoMoPaymentResponse;

public interface PaymentGatewayStrategy {
    String providerCode();

    MoMoPaymentResponse createPayment(Payment payment, Ticket ticket);

    boolean verifyIpnSignature(Map<String, Object> ipnPayload);

    boolean verifyReturnSignature(Map<String, String> params);

    boolean isSuccessResult(String resultCode);
}
