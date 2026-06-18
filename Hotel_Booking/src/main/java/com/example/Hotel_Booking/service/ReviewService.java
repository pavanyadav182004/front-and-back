package com.example.Hotel_Booking.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Hotel_Booking.dto.ReviewDTO;
import com.example.Hotel_Booking.entity.Booking;
import com.example.Hotel_Booking.entity.Review;
import com.example.Hotel_Booking.enums.BookingStatus;
import com.example.Hotel_Booking.exception.CustomException;
import com.example.Hotel_Booking.repository.BookingRepository;
import com.example.Hotel_Booking.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository,
            EmailService emailService) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    public Map<String, Object> addReview(ReviewDTO dto, String email) {
        if (email == null) {
            throw new CustomException("Please login to review");
        }
        if (dto.getBookingId() == null) {
            throw new CustomException("Booking ID is required");
        }
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new CustomException("Rating must be between 1 and 5");
        }
        if (dto.getComment() == null || dto.getComment().isBlank()) {
            throw new CustomException("Comment is required");
        }

        @SuppressWarnings("null")
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new CustomException("Booking not found"));

        if (!booking.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new CustomException("You can review only your own booking");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new CustomException("Review is allowed only after confirmed booking");
        }
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new CustomException("Review already submitted for this booking");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setHotel(booking.getHotel());
        review.setUser(booking.getUser());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment().trim());

        Review savedReview = reviewRepository.save(review);

        // 🔥 Send email notification
        emailService.sendReviewConfirmation(savedReview);

        return toResponse(savedReview);
    }

    public List<Map<String, Object>> getHotelReviews(Long hotelId) {
        return reviewRepository.findByHotelIdOrderByIdDesc(hotelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<Map<String, Object>> getAllReviews() {
        return reviewRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Map<String, Object> toResponse(Review review) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", review.getId());
        data.put("bookingId", review.getBooking().getId());
        data.put("hotelId", review.getHotel().getId());
        data.put("hotelName", review.getHotel().getName());
        data.put("userName", review.getUser().getName());
        data.put("userImage", review.getUser().getImageUrl());
        data.put("userAddress", review.getUser().getAddress());
        data.put("rating", review.getRating());
        data.put("comment", review.getComment());
        data.put("createdAt", review.getCreatedAt());
        return data;
    }
}
