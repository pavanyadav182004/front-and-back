package com.example.Hotel_Booking.service;

import org.springframework.stereotype.Service;

import com.example.Hotel_Booking.entity.Booking;
import com.example.Hotel_Booking.entity.Payment;
import com.example.Hotel_Booking.enums.BookingStatus;
import com.example.Hotel_Booking.enums.PaymentStatus;
import com.example.Hotel_Booking.exception.CustomException;
import com.example.Hotel_Booking.repository.BookingRepository;
import com.example.Hotel_Booking.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final RazorpayClient razorpayClient;
    private final EmailService emailService;

    public PaymentService(PaymentRepository paymentRepo, BookingRepository bookingRepo, RazorpayClient razorpayClient, EmailService emailService) {
        this.paymentRepo = paymentRepo;
        this.bookingRepo = bookingRepo;
        this.razorpayClient = razorpayClient;
        this.emailService = emailService;
    }

    public String createOrder(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found"));

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(booking.getTotalPrice() * 100)); // amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_rcpt_" + bookingId);

            Order order = razorpayClient.orders.create(orderRequest);
            return order.toString();
        } catch (RazorpayException e) {
            throw new CustomException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    public com.example.Hotel_Booking.entity.Payment makePayment(Long bookingId, String razorpayOrderId) {

        if (bookingId == null) {
            throw new CustomException("Booking ID is required");
        }

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found"));

        if (paymentRepo.existsByBookingId(bookingId)) {
            throw new CustomException("Payment already done for this booking");
        }

        // Record the payment
        com.example.Hotel_Booking.entity.Payment payment = new com.example.Hotel_Booking.entity.Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayOrderId(razorpayOrderId);
        paymentRepo.save(payment);

        // Confirm the booking
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepo.save(booking);

        // Send confirmation email
        try {
            emailService.sendBookingConfirmation(booking);
        } catch (Exception e) {
            System.err.println("Failed to send booking confirmation email: " + e.getMessage());
        }

        return payment;
    }
}
