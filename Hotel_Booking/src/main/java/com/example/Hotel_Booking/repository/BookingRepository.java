package com.example.Hotel_Booking.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Hotel_Booking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserEmailOrderByIdDesc(String email);

    List<Booking> findByHotelId(Long hotelId);

    List<Booking> findAllByOrderByIdDesc();

    List<Booking> findByStatusAndCheckOutLessThanEqual(com.example.Hotel_Booking.enums.BookingStatus status, LocalDate date);

    @Query("SELECT COALESCE(SUM(b.numOfRooms), 0) FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.status <> 'CANCELLED'")
    Long countActiveBookedRoomsByRoomId(@Param("roomId") Long roomId);

    /**
     * Detects overlap: any booking for the same room where
     * the existing interval [checkIn, checkOut) overlaps [newIn, newOut).
     * Overlap condition: existingCheckIn < newCheckOut AND existingCheckOut > newCheckIn
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.checkIn < :checkOut " +
           "AND b.checkOut > :checkIn")
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.checkIn < :checkOut " +
           "AND b.checkOut > :checkIn")
    long countOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.user.id = :userId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.checkIn < :checkOut " +
           "AND b.checkOut > :checkIn")
    boolean existsByRoomIdAndUserIdAndDates(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
