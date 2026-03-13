package com.cinebee.domain.entity;

import jakarta.persistence.*;
import com.cinebee.domain.entity.base.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Payments", indexes = {
        @Index(name = "idx_orderid", columnList = "orderId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(unique = true)
    private String orderId; // Unique ID for the transaction in our system

    private String requestId; // ID for the specific request to the payment provider

    private String provider; // e.g., "MOMO", "VNPAY"

    public enum PaymentMethod {
        CASH, CARD, MOMO, VNPAY
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, CANCELED
    }

    public Payment(Ticket ticket, User user, String orderId, String requestId) {
        this.ticket = ticket;
        this.user = user;
        this.amount = ticket.getPrice();
        this.orderId = orderId;
        this.requestId = requestId;
        this.paymentMethod = PaymentMethod.MOMO;
        this.provider = "MOMO";
        this.paymentStatus = PaymentStatus.PENDING;
    }
}

