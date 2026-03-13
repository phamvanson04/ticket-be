package com.cinebee.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Room_Seats", uniqueConstraints = @UniqueConstraint(columnNames = { "room_id", "seat_number" }))
public class RoomSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType seatType = SeatType.STANDARD;

    public enum SeatType {
        STANDARD, VIP, PREMIUM
    }
    // ...getter, setter...
}
