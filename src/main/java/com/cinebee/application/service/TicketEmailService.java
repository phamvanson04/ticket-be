package com.cinebee.application.service;

import com.cinebee.domain.entity.Ticket;

public interface TicketEmailService {
    void sendTicketConfirmationEmail(Ticket ticket);
}

