package com.example.Hotel_Booking.dto;

public class VerifyOtpDTO {
    private String email;
    private String otp;
    private String role;

    public VerifyOtpDTO() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
