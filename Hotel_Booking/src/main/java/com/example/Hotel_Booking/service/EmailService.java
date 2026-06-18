package com.example.Hotel_Booking.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    public void sendOtp(String to, String subject, String otp) {
        String body = "Your Hotel Booking OTP is " + otp + ". It is valid for 5 minutes.";
        sendEmail(to, subject, body, false);
    }

    public void sendBookingConfirmation(com.example.Hotel_Booking.entity.Booking booking) {
        String to = booking.getUser().getEmail();
        String subject = "Booking Confirmed - " + booking.getHotel().getName();
        
        String htmlBody = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;'>" +
                "<h1 style='color: #2563eb; text-align: center;'>Booking Confirmed! ✅</h1>" +
                "<p>Hi " + booking.getUser().getName() + ",</p>" +
                "<p>Your booking at <strong>" + booking.getHotel().getName() + "</strong> has been successfully confirmed.</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee;'>" +
                "<h3>Booking Details:</h3>" +
                "<table style='width: 100%;'>" +
                "<tr><td><strong>Booking ID:</strong></td><td>#" + booking.getId() + "</td></tr>" +
                "<tr><td><strong>Location:</strong></td><td>" + booking.getHotel().getAddress() + ", " + booking.getHotel().getCity() + "</td></tr>" +
                "<tr><td><strong>Check-In:</strong></td><td>" + booking.getCheckIn() + "</td></tr>" +
                "<tr><td><strong>Check-Out:</strong></td><td>" + booking.getCheckOut() + "</td></tr>" +
                "<tr><td><strong>Guests:</strong></td><td>" + booking.getGuests() + "</td></tr>" +
                "<tr><td><strong>Room Type:</strong></td><td>" + booking.getRoom().getType() + "</td></tr>" +
                "<tr><td><strong>Total Paid:</strong></td><td style='color: #16a34a; font-weight: bold;'>₹" + booking.getTotalPrice() + "</td></tr>" +
                "</table>" +
                "<br><p style='text-align: center; color: #666; font-size: 12px;'>Thank you for choosing us!</p>" +
                "</div></body></html>";

        sendEmail(to, subject, htmlBody, true);
    }

    public void sendBookingCancellation(com.example.Hotel_Booking.entity.Booking booking) {
        String to = booking.getUser().getEmail();
        String subject = "Booking Cancelled - " + booking.getHotel().getName();
        
        String htmlBody = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;'>" +
                "<h1 style='color: #dc2626; text-align: center;'>Booking Cancelled ❌</h1>" +
                "<p>Hi " + booking.getUser().getName() + ",</p>" +
                "<p>Your booking at <strong>" + booking.getHotel().getName() + "</strong> has been cancelled as requested.</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee;'>" +
                "<h3>Cancelled Booking Details:</h3>" +
                "<table style='width: 100%;'>" +
                "<tr><td><strong>Booking ID:</strong></td><td>#" + booking.getId() + "</td></tr>" +
                "<tr><td><strong>Location:</strong></td><td>" + booking.getHotel().getAddress() + ", " + booking.getHotel().getCity() + "</td></tr>" +
                "<tr><td><strong>Check-In:</strong></td><td>" + booking.getCheckIn() + "</td></tr>" +
                "<tr><td><strong>Check-Out:</strong></td><td>" + booking.getCheckOut() + "</td></tr>" +
                "<tr><td><strong>Total Amount:</strong></td><td>₹" + booking.getTotalPrice() + "</td></tr>" +
                "</table>" +
                "<br><p style='text-align: center; color: #666; font-size: 12px;'>If you have already paid, your payment will be refunded within 5-7 business days.</p>" +
                "</div></body></html>";

        sendEmail(to, subject, htmlBody, true);
    }

    public void sendCheckoutEmail(com.example.Hotel_Booking.entity.Booking booking) {
        String to = booking.getUser().getEmail();
        String subject = "Checkout Successful - " + booking.getHotel().getName();
        
        String htmlBody = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;'>" +
                "<h1 style='color: #2563eb; text-align: center;'>Booking Expired & Checked Out ✅</h1>" +
                "<p>Hi " + booking.getUser().getName() + ",</p>" +
                "<p>Your scheduled check-out date for the room at <strong>" + booking.getHotel().getName() + "</strong> has arrived, and you have been automatically checked out.</p>" +
                "<p>If you wish to stay longer, please renew your booking or make a new reservation!</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee;'>" +
                "<h3>Booking History:</h3>" +
                "<table style='width: 100%;'>" +
                "<tr><td><strong>Booking ID:</strong></td><td>#" + booking.getId() + "</td></tr>" +
                "<tr><td><strong>Check-In:</strong></td><td>" + booking.getCheckIn() + "</td></tr>" +
                "<tr><td><strong>Check-Out:</strong></td><td>" + booking.getCheckOut() + "</td></tr>" +
                "</table>" +
                "<br><p style='text-align: center; color: #666; font-size: 12px;'>Thank you for choosing us! Hope to see you again.</p>" +
                "</div></body></html>";

        sendEmail(to, subject, htmlBody, true);
    }

    public void sendReviewConfirmation(com.example.Hotel_Booking.entity.Review review) {
        String to = review.getUser().getEmail();
        String subject = "Thank you for your review! - " + review.getHotel().getName();
        
        String htmlBody = "<html><body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='max-width: 600px; margin: auto; border: 1px solid #ddd; padding: 20px; border-radius: 10px;'>" +
                "<h1 style='color: #2563eb; text-align: center;'>Review Submitted! ⭐</h1>" +
                "<p>Hi " + review.getUser().getName() + ",</p>" +
                "<p>Thank you for sharing your experience at <strong>" + review.getHotel().getName() + "</strong>.</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee;'>" +
                "<h3>Your Review:</h3>" +
                "<p><strong>Rating:</strong> " + review.getRating() + " / 5</p>" +
                "<p><strong>Comment:</strong> " + review.getComment() + "</p>" +
                "<br><p style='text-align: center; color: #666; font-size: 12px;'>We value your feedback and hope to see you again soon!</p>" +
                "</div></body></html>";

        sendEmail(to, subject, htmlBody, true);
    }

    private void sendEmail(String to, String subject, String body, boolean isHtml) {
        String apiKey = brevoApiKey != null ? brevoApiKey.trim() : "";
        String from = senderEmail != null ? senderEmail.trim() : "";

        if (apiKey.isEmpty()) {
            System.err.println("Email skipped: BREVO_API_KEY not configured");
            return;
        }
        if (from.isEmpty()) {
            from = "pavanyadav182004@gmail.com";
        }

        try {
            URL url = new URL("https://api.brevo.com/v3/smtp/email");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setRequestProperty("content-type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Escape special characters for JSON
            String escapedBody = body.replace("\\", "\\\\")
                                     .replace("\"", "\\\"")
                                     .replace("\n", "\\n")
                                     .replace("\r", "\\r")
                                     .replace("\t", "\\t");
            String escapedSubject = subject.replace("\\", "\\\\")
                                           .replace("\"", "\\\"");

            String contentType = isHtml ? "text/html" : "text/plain";

            String jsonPayload = "{"
                    + "\"sender\":{\"name\":\"Hotel Booking\",\"email\":\"" + from + "\"},"
                    + "\"to\":[{\"email\":\"" + to + "\"}],"
                    + "\"subject\":\"" + escapedSubject + "\","
                    + (isHtml
                        ? "\"htmlContent\":\"" + escapedBody + "\""
                        : "\"textContent\":\"" + escapedBody + "\"")
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Email sent successfully to " + to);
            } else {
                // Read error response
                java.io.InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    String errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    System.err.println("Brevo email failed (HTTP " + responseCode + "): " + errorBody);
                } else {
                    System.err.println("Brevo email failed with HTTP " + responseCode);
                }
            }
            conn.disconnect();
        } catch (Exception ex) {
            // Log error but don't break the flow
            System.err.println("Email failed: " + ex.getMessage());
        }
    }
}
