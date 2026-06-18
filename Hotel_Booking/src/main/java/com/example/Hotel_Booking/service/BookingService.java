package com.example.Hotel_Booking.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.Hotel_Booking.dto.BookingDTO;
import com.example.Hotel_Booking.entity.Booking;
import com.example.Hotel_Booking.entity.Hotel;
import com.example.Hotel_Booking.entity.Room;
import com.example.Hotel_Booking.entity.User;
import com.example.Hotel_Booking.enums.BookingStatus;
import com.example.Hotel_Booking.exception.CustomException;
import com.example.Hotel_Booking.repository.BookingRepository;
import com.example.Hotel_Booking.repository.ReviewRepository;
import com.example.Hotel_Booking.repository.RoomRepository;
import com.example.Hotel_Booking.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;
    private final EmailService emailService;

    public BookingService(BookingRepository bookingRepo,
            RoomRepository roomRepo,
            UserRepository userRepo,
            ReviewRepository reviewRepo,
            EmailService emailService) {
        this.bookingRepo = bookingRepo;
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
        this.emailService = emailService;
    }

    // ─────────────────────────────────────────────
    // CREATE BOOKING
    // ─────────────────────────────────────────────
    @Transactional
    public Map<String, Object> createBooking(BookingDTO dto, HttpServletRequest request) {

        // 🔥 FIX: safe user extraction
        String email = (String) request.getAttribute("email");

        if (email == null) {
            throw new CustomException("User not authenticated");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        // Validation
        if (dto.getRoomId() == null)
            throw new CustomException("room_id is required");

        if (dto.getCheckIn() == null || dto.getCheckOut() == null)
            throw new CustomException("check_in and check_out required");

        if (dto.getGuests() < 1)
            throw new CustomException("guests must be at least 1");

        LocalDate today = LocalDate.now();

        if (dto.getCheckIn().isBefore(today))
            throw new CustomException("Check-in cannot be past");

        if (!dto.getCheckOut().isAfter(dto.getCheckIn()))
            throw new CustomException("Invalid date range");

        // Room fetch
        @SuppressWarnings("null")
        Room room = roomRepo.findById(dto.getRoomId())
                .orElseThrow(() -> new CustomException("Room not found"));

        if (!room.isAvailable())
            throw new CustomException("Room not available");

        Hotel hotel = room.getHotel();

        // 🔥 Check if the HOTEL itself is available
        if (!hotel.isAvailable())
            throw new CustomException("This hotel is currently not available for booking. Please try another hotel.");

        // 1. Check if THIS USER already booked THIS ROOM for these dates
        if (bookingRepo.existsByRoomIdAndUserIdAndDates(room.getId(), user.getId(), dto.getCheckIn(),
                dto.getCheckOut())) {
            throw new CustomException("You have already booked this room for the selected dates.");
        }

        // 2. Check total availability for these dates
        long overlappingBookings = bookingRepo.countOverlappingBookings(
                room.getId(), dto.getCheckIn(), dto.getCheckOut());

        if (overlappingBookings >= room.getTotalRooms())
            throw new CustomException(
                    "All rooms of this type are already booked for the selected dates. Please try another room or date.");

        // Price calculation
        long nights = ChronoUnit.DAYS.between(dto.getCheckIn(), dto.getCheckOut());
        if (nights < 1)
            throw new CustomException("Minimum 1 night required");
        if (nights > 20)
            throw new CustomException("You cannot book a room for more than 20 days.");

        double totalPrice = nights * room.getPrice();

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user); // 🔥 CORRECT USER
        booking.setHotel(hotel);
        booking.setRoom(room);
        booking.setCheckIn(dto.getCheckIn());
        booking.setCheckOut(dto.getCheckOut());
        booking.setGuests(dto.getGuests());
        booking.setNumOfRooms(1); // FIXED FIELD
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepo.save(booking);
        syncBookedRooms(room);

        return toBookingResponse(savedBooking);
    }

    // ─────────────────────────────────────────────
    // CANCEL BOOKING
    // ─────────────────────────────────────────────
    @Transactional
    public Map<String, Object> cancelBooking(Long id, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        if (email == null) {
            throw new CustomException("User not authenticated");
        }

        @SuppressWarnings("null")
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new CustomException("Booking not found"));

        // Ensure the booking belongs to the current user
        if (!booking.getUser().getEmail().equals(email)) {
            throw new CustomException("Unauthorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new CustomException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);

        // Sync booked rooms to free up availability
        syncBookedRooms(booking.getRoom());

        // Send Cancellation Email
        emailService.sendBookingCancellation(booking);

        return toBookingResponse(booking);
    }

    // ─────────────────────────────────────────────
    // CHECKOUT BOOKING
    // ─────────────────────────────────────────────
    @Transactional
    public Map<String, Object> checkoutBooking(Long id, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        if (email == null) {
            throw new CustomException("User not authenticated");
        }

        @SuppressWarnings("null")
        Booking booking = bookingRepo.findById(id)
                .orElseThrow(() -> new CustomException("Booking not found"));

        User user = userRepo.findByEmail(email).orElseThrow(() -> new CustomException("User not found"));

        if (!booking.getUser().getEmail().equals(email) && !user.getRole().name().equals("ADMIN")
                && !user.getRole().name().equals("HOTEL_OWNER")) {
            throw new CustomException("Unauthorized to checkout this booking");
        }

        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new CustomException("Booking is already checked out");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new CustomException("Cancelled bookings cannot be checked out");
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepo.save(booking);

        // Sync booked rooms to free up availability
        syncBookedRooms(booking.getRoom());

        // Send Checkout Email
        emailService.sendCheckoutEmail(booking);

        return toBookingResponse(booking);
    }

    // ─────────────────────────────────────────────
    // AUTO CHECKOUT BOOKINGS (Runs Every Day at 12 PM)
    // ─────────────────────────────────────────────
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void autoCheckoutBookings() {
        List<Booking> expiredBookings = bookingRepo.findByStatusAndCheckOutLessThanEqual(
                BookingStatus.CONFIRMED, LocalDate.now());

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CHECKED_OUT);
            bookingRepo.save(booking);
            syncBookedRooms(booking.getRoom());

            try {
                emailService.sendCheckoutEmail(booking);
            } catch (Exception e) {
                System.err.println("Failed to send checkout email for booking " + booking.getId());
            }
        }
        System.out.println("Auto-checked out " + expiredBookings.size() + " bookings.");
    }

    // ─────────────────────────────────────────────
    // USER BOOKINGS
    // ─────────────────────────────────────────────
    public List<Map<String, Object>> getBookingsForUser(HttpServletRequest request) {

        String email = (String) request.getAttribute("email");

        if (email == null) {
            throw new CustomException("User not authenticated");
        }

        return bookingRepo.findByUserEmailOrderByIdDesc(email)
                .stream()
                .map(this::toBookingResponse)
                .toList();
    }

    // ─────────────────────────────────────────────
    // ADMIN BOOKINGS
    // ─────────────────────────────────────────────
    public List<Map<String, Object>> getAllBookings() {
        return bookingRepo.findAllByOrderByIdDesc()
                .stream()
                .map(this::toBookingResponse)
                .toList();
    }

    private Map<String, Object> toBookingResponse(Booking booking) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", booking.getId());
        data.put("checkIn", booking.getCheckIn());
        data.put("checkOut", booking.getCheckOut());
        data.put("guests", booking.getGuests());
        data.put("numOfRooms", booking.getNumOfRooms());
        data.put("totalPrice", booking.getTotalPrice());
        data.put("status", booking.getStatus());
        data.put("reviewed", reviewRepo.existsByBookingId(booking.getId()));

        User user = booking.getUser();
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        data.put("user", userData);

        Hotel hotel = booking.getHotel();
        Map<String, Object> hotelData = new HashMap<>();
        hotelData.put("id", hotel.getId());
        hotelData.put("name", hotel.getName());
        hotelData.put("city", hotel.getCity());
        hotelData.put("address", hotel.getAddress());
        hotelData.put("imageUrl",
                (hotel.getImages() != null && !hotel.getImages().isEmpty() ? hotel.getImages().get(0) : null));
        data.put("hotel", hotelData);

        Room room = booking.getRoom();
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("id", room.getId());
        roomData.put("type", room.getType());
        roomData.put("price", room.getPrice());
        roomData.put("personCount", room.getPersonCount());
        data.put("room", roomData);

        return data;
    }

    private void syncBookedRooms(Room room) {
        Long activeBookedRooms = bookingRepo.countActiveBookedRoomsByRoomId(room.getId());
        int bookedRooms = activeBookedRooms == null ? 0 : activeBookedRooms.intValue();
        room.setBookedRooms(bookedRooms);
        room.setAvailable(bookedRooms < room.getTotalRooms());
        roomRepo.save(room);
    }
}
