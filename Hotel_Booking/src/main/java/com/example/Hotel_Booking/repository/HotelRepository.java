package com.example.Hotel_Booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Hotel_Booking.entity.Hotel;

public interface HotelRepository  extends JpaRepository<Hotel, Long>{

	List<Hotel> findByCityContainingIgnoreCase(String city);
	
}
