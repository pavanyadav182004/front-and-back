package com.example.Hotel_Booking.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Hotel_Booking.entity.User;
import com.example.Hotel_Booking.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    // Email se user find karne ke liye
    Optional<User> findByEmail(String email);

    // 🔥 Admin exist check karne ke liye
    boolean existsByRole(Role role);
}