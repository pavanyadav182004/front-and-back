package com.example.Hotel_Booking.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.entity.Hotel;
import com.example.Hotel_Booking.service.HotelService;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "*")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseDTO getAllHotels() {
        return new ResponseDTO(true, "Hotels fetched successfully", hotelService.getAllHotels());
    }

    @GetMapping("/search")
    public ResponseDTO searchHotels(@RequestParam(defaultValue = "") String city) {
        return new ResponseDTO(true, "Hotels fetched successfully", hotelService.searchByCity(city));
    }

    @GetMapping("/{id}")
    public ResponseDTO getHotel(@PathVariable Long id) {
        return new ResponseDTO(true, "Hotel fetched successfully", hotelService.getHotel(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO addHotel(@RequestBody Hotel hotel) {
        return new ResponseDTO(true, "Hotel added successfully", hotelService.addHotel(hotel));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO updateHotel(@PathVariable Long id, @RequestBody Hotel hotel) {
        return new ResponseDTO(true, "Hotel updated successfully", hotelService.updateHotel(id, hotel));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return new ResponseDTO(true, "Hotel deleted successfully", null);
    }
}
