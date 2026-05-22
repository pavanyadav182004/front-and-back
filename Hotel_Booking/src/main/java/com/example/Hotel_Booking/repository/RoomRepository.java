package com.example.Hotel_Booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Hotel_Booking.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelId(Long hotelId);

    Optional<Room> findFirstByHotelIdOrderByIdAsc(Long hotelId);

    /**
     * Returns rooms for a hotel that are NOT already booked
     * during the requested date range.
     */
    @Query("SELECT r FROM Room r " +
           "WHERE r.hotel.id = :hotelId " +
           "AND r.available = true " +
           "AND (" +
           "   SELECT COUNT(b) FROM Booking b " +
           "   WHERE b.room.id = r.id " +
           "   AND b.status <> 'CANCELLED' " +
           "   AND b.checkIn < :checkOut " +
           "   AND b.checkOut > :checkIn" +
           ") < r.totalRooms")
    List<Room> findAvailableRooms(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
