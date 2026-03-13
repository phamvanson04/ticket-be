package com.cinebee.application.service.impl;

import com.cinebee.domain.entity.Ticket;
import com.cinebee.infrastructure.persistence.repository.TicketRepository;
import com.cinebee.application.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class QRCodeServiceImpl implements QRCodeService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public BufferedImage generateTicketQRCode(Long ticketId) {
        try {
            String qrData = generateQRCodeData(ticketId);
            
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);
            
            BufferedImage qrImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = qrImage.createGraphics();
            
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 300, 300);
            graphics.setColor(Color.BLACK);
            
            for (int i = 0; i < 300; i++) {
                for (int j = 0; j < 300; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            
            graphics.dispose();
            return qrImage;
            
        } catch (Exception e) {
            log.error("Error generating QR code for ticket {}: {}", ticketId, e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    @Override
    public String generateQRCodeData(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
            
        // Táº¡o data cho QR code (cÃ³ thá»ƒ encrypt náº¿u cáº§n)
        return String.format(
            "CINEBEE_TICKET|ID:%d|MOVIE:%s|THEATER:%s|SHOWTIME:%s|SEAT:%s|PRICE:%.0f",
            ticket.getId(),
            ticket.getShowtime().getMovie().getTitle(),
            ticket.getShowtime().getRoom().getTheater().getName(),
            ticket.getShowtime().getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
            ticket.getSeat().getSeatNumber(), // Single seat
            ticket.getPrice()
        );
    }
}

