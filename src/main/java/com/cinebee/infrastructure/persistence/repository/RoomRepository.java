package com.cinebee.infrastructure.persistence.repository;

import com.cinebee.domain.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Find rooms by theater
    List<Room> findByTheaterId(Long theaterId);
    
    // Find rooms by type
    List<Room> findByType(String type);
    
    // Find rooms by theater and type
    List<Room> findByTheaterIdAndType(Long theaterId, String type);
    
    // Find room by theater and name
    Optional<Room> findByTheaterIdAndName(Long theaterId, String name);
    
    // Find available rooms for a theater (rooms that exist)
    @Query("SELECT r FROM Room r WHERE r.theater.id = :theaterId AND r.isActive = true")
    List<Room> findActiveRoomsByTheater(@Param("theaterId") Long theaterId);
    
    // Count total rooms in a theater
    long countByTheaterId(Long theaterId);
    
    // Find rooms with capacity greater than or equal to specified value
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
    
    // Find rooms by theater with capacity range
    @Query("SELECT r FROM Room r WHERE r.theater.id = :theaterId AND r.capacity BETWEEN :minCapacity AND :maxCapacity")
    List<Room> findByTheaterIdAndCapacityBetween(@Param("theaterId") Long theaterId, 
                                                @Param("minCapacity") Integer minCapacity, 
                                                @Param("maxCapacity") Integer maxCapacity);
}

