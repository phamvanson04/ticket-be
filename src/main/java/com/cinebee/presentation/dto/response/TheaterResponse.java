package com.cinebee.presentation.dto.response;

import com.cinebee.domain.entity.Room;
import com.cinebee.domain.entity.Theater;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class TheaterResponse {
    private Long id;
    private String name;
    private String address;
    private String contactInfo;
    private String openingHours;
    private String status;
    private LocalDateTime createdAt;
    private List<RoomInfo> rooms;

    @Getter
    @Builder
    public static class RoomInfo {
        private Long id;
        private String name;
        private Integer capacity;
    }

    public static TheaterResponse fromEntity(Theater theater) {
        List<RoomInfo> roomInfos = theater.getRooms().stream()
                .map(room -> RoomInfo.builder()
                        .id(room.getId())
                        .name(room.getName())
                        .capacity(room.getCapacity())
                        .build())
                .collect(Collectors.toList());

        return TheaterResponse.builder()
                .id(theater.getId())
                .name(theater.getName())
                .address(theater.getAddress())
                .contactInfo(theater.getContactInfo())
                .openingHours(theater.getOpeningHours())
                .status(theater.getStatus().name())
                .createdAt(theater.getCreatedAt())
                .rooms(roomInfos)
                .build();
    }
}

