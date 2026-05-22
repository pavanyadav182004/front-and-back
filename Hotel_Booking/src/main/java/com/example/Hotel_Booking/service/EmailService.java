package com.example.Hotel_Booking.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.port:465}")
    private int port;

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
        String configuredUsername = clean(username);
        String configuredPassword = clean(password).replace(" ", "");

        if (configuredUsername.isBlank() || configuredPassword.isBlank()
                || "yourgmail@gmail.com".equalsIgnoreCase(configuredUsername)
                || "your-16-character-app-password".equalsIgnoreCase(configuredPassword)) {
            return; // Silently skip if not configured, or log it
        }

        try (
                SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            expectOk(reader);
            send(writer, reader, "EHLO localhost");
            send(writer, reader, "AUTH LOGIN");
            send(writer, reader, Base64.getEncoder().encodeToString(configuredUsername.getBytes(StandardCharsets.UTF_8)));
            send(writer, reader, Base64.getEncoder().encodeToString(configuredPassword.getBytes(StandardCharsets.UTF_8)));
            send(writer, reader, "MAIL FROM:<" + configuredUsername + ">");
            send(writer, reader, "RCPT TO:<" + to + ">");
            send(writer, reader, "DATA");

            writer.write("From: " + configuredUsername + "\r\n");
            writer.write("To: " + to + "\r\n");
            writer.write("Subject: " + subject + "\r\n");
            if (isHtml) {
                writer.write("Content-Type: text/html; charset=UTF-8\r\n");
            } else {
                writer.write("Content-Type: text/plain; charset=UTF-8\r\n");
            }
            writer.write("\r\n");
            writer.write(body + "\r\n");
            writer.write(".\r\n");
            writer.flush();
            expectOk(reader);
            send(writer, reader, "QUIT");
        } catch (Exception ex) {
            // Log error but don't break the payment flow
            System.err.println("Email failed: " + ex.getMessage());
        }
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            return cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    private void send(BufferedWriter writer, BufferedReader reader, String command) throws Exception {
        writer.write(command + "\r\n");
        writer.flush();
        expectOk(reader);
    }

    private void expectOk(BufferedReader reader) throws Exception {
        String line = reader.readLine();
        if (line == null) {
            throw new RuntimeException("SMTP server closed the connection");
        }

        String code = line.length() >= 3 ? line.substring(0, 3) : "";
        while (line.length() > 3 && line.charAt(3) == '-') {
            line = reader.readLine();
            if (line == null) {
                throw new RuntimeException("SMTP server closed the connection");
            }
        }

        if (!code.startsWith("2") && !code.startsWith("3")) {
            throw new RuntimeException("SMTP error: " + line);
        }
    }
}
