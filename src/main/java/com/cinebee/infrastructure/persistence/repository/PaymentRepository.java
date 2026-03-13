package com.cinebee.infrastructure.persistence.repository;

import com.cinebee.domain.entity.Payment;
import com.cinebee.domain.entity.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    boolean existsByTicketAndPaymentStatus(Ticket ticket, Payment.PaymentStatus status);

    boolean existsByTicketInAndPaymentStatus(List<Ticket> tickets, Payment.PaymentStatus status);
}

