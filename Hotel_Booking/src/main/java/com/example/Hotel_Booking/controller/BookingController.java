package com.example.Hotel_Booking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Hotel_Booking.dto.BookingDTO;
import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.entity.Room;
import com.example.Hotel_Booking.repository.RoomRepository;
import com.example.Hotel_Booking.service.BookingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final RoomRepository roomRepo;

    public BookingController(BookingService bookingService, RoomRepository roomRepo) {
        this.bookingService = bookingService;
        this.roomRepo = roomRepo;
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/bookings/available?hotelId=1&checkIn=2025-06-01&checkOut=2025-06-05
    // Public: returns rooms that are NOT yet booked for those dates
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/available")
    public ResponseDTO getAvailableRooms(
             @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return new ResponseDTO(false, "Invalid date range", null);
        }
        List<Room> available = roomRepo.findAvailableRooms(hotelId, checkIn, checkOut);
        return new ResponseDTO(true, "Available rooms fetched", available);
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/bookings  →  create booking (USER / ADMIN)
    // ──────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseDTO createBooking(@Valid @RequestBody BookingDTO dto,
                                     HttpServletRequest request) {
        return new ResponseDTO(
                true,
                "Booking created successfully. Please complete payment to confirm.",
                bookingService.createBooking(dto, request)
        );
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/bookings/user  →  current user's bookings
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/user")
    public ResponseDTO getUserBookings(HttpServletRequest request) {
        return new ResponseDTO(true, "Bookings fetched successfully",
                bookingService.getBookingsForUser(request));
    }

    // ──────────────────────────────────────────────────────────────
    // PATCH /api/bookings/{id}/cancel  →  cancel booking
    // ──────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/cancel")
    public ResponseDTO cancelBooking(@PathVariable Long id, HttpServletRequest request) {
        return new ResponseDTO(true, "Booking cancelled successfully",
                bookingService.cancelBooking(id, request));
    }

    // ──────────────────────────────────────────────────────────────
    // PATCH /api/bookings/{id}/checkout  →  checkout booking
    // ──────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/checkout")
    public ResponseDTO checkoutBooking(@PathVariable Long id, HttpServletRequest request) {
        return new ResponseDTO(true, "Booking checked out successfully",
                bookingService.checkoutBooking(id, request));
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/bookings  →  all bookings (ADMIN)
    // ──────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO getAllBookings() {
        return new ResponseDTO(true, "Bookings fetched successfully",
                bookingService.getAllBookings());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO getAdminBookings() {
        return new ResponseDTO(true, "Bookings fetched successfully",
                bookingService.getAllBookings());
    }
}
