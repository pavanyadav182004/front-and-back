package com.example.Hotel_Booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Hotel_Booking.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	 boolean existsByBookingId(Long bookingId);

     void deleteByBookingId(Long bookingId);
}
