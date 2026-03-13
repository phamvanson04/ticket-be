package com.cinebee.application.service;

import com.cinebee.domain.entity.Ticket;

public interface TicketConfirmationEmailService {
    void sendTicketConfirmationEmail(Ticket ticket);
}

