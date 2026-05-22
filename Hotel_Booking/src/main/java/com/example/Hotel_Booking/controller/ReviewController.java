package com.example.Hotel_Booking.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.dto.ReviewDTO;
import com.example.Hotel_Booking.service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseDTO addReview(@RequestBody ReviewDTO dto, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return new ResponseDTO(true, "Review submitted successfully", reviewService.addReview(dto, email));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseDTO getHotelReviews(@PathVariable Long hotelId) {
        return new ResponseDTO(true, "Reviews fetched successfully", reviewService.getHotelReviews(hotelId));
    }

    @GetMapping
    public ResponseDTO getAllReviews() {
        return new ResponseDTO(true, "All reviews fetched successfully", reviewService.getAllReviews());
    }
}
