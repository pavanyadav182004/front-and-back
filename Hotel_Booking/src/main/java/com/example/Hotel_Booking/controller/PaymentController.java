package com.example.Hotel_Booking.controller;

import org.springframework.web.bind.annotation.*;

import com.example.Hotel_Booking.dto.PaymentDTO;
import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments
     * Body: { "bookingId": 5 }
     * Marks payment SUCCESS and sets booking status → CONFIRMED
     */
    @PostMapping
    public ResponseDTO pay(@RequestBody PaymentDTO dto) {
        return new ResponseDTO(
                true,
                "Payment successful. Booking confirmed!",
                paymentService.makePayment(dto.getBookingId(), dto.getRazorpayOrderId()));
    }

    @PostMapping("/create-order/{bookingId}")
    public ResponseDTO createOrder(@PathVariable Long bookingId) {
        try {
            return new ResponseDTO(true, "Order created", paymentService.createOrder(bookingId));
        } catch (Exception e) {
            return new ResponseDTO(false, e.getMessage(), null);
        }
    }
}
