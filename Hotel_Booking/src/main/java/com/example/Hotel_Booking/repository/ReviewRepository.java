package com.example.Hotel_Booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Hotel_Booking.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByBookingId(Long bookingId);
    Optional<Review> findByBookingId(Long bookingId);
    List<Review> findByHotelIdOrderByIdDesc(Long hotelId);
    List<Review> findAllByOrderByIdDesc();
    void deleteByBookingId(Long bookingId);
}
