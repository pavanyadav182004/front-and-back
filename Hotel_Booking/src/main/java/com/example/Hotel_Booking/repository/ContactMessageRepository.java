package com.example.Hotel_Booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Hotel_Booking.entity.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
