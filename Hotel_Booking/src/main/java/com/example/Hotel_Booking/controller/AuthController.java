package com.example.Hotel_Booking.controller;

import org.springframework.web.bind.annotation.*;

import com.example.Hotel_Booking.dto.LoginDTO;
import com.example.Hotel_Booking.dto.EmailOtpDTO;
import com.example.Hotel_Booking.dto.ForgotPasswordDTO;
import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.dto.ResetPasswordDTO;
import com.example.Hotel_Booking.dto.UserDTO;
import com.example.Hotel_Booking.dto.VerifyOtpDTO;
import com.example.Hotel_Booking.service.AuthService;
import com.example.Hotel_Booking.repository.UserRepository;
import com.example.Hotel_Booking.enums.Role;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // Constructor Injection (✔️ @Autowired not needed)
    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    // ================= USER REGISTER =================
    @PostMapping("/register/send-otp")
    public ResponseDTO sendRegisterOtp(@RequestBody EmailOtpDTO dto) {
        return authService.sendRegistrationOtp(dto.getEmail(), dto.getRole());
    }

    @PostMapping("/register/verify-otp")
    public ResponseDTO verifyRegisterOtp(@RequestBody VerifyOtpDTO dto) {
        return authService.verifyRegistrationOtp(dto.getEmail(), dto.getOtp(), dto.getRole());
    }

    @PostMapping("/register")
    public ResponseDTO register(@RequestBody UserDTO dto) {
        return authService.registerUser(dto);
    }

    // ================= ADMIN REGISTER =================
    @PostMapping("/admin/register")
    public ResponseDTO registerAdmin(@RequestBody UserDTO dto) {
        return authService.registerAdmin(dto);
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseDTO login(@RequestBody LoginDTO dto) {
        return authService.login(dto.getEmail(), dto.getPassword());
    }

    @PostMapping("/forgot-password")
    public ResponseDTO forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        return authService.forgotPassword(dto.getEmail());
    }

    @PostMapping("/reset-password")
    public ResponseDTO resetPassword(@RequestBody ResetPasswordDTO dto) {
        return authService.resetPassword(dto.getEmail(), dto.getOtp(), dto.getNewPassword());
    }

    // ================= ADMIN EXISTS =================
    @GetMapping("/admin-exists")
    public boolean adminExists() {
        return userRepository.existsByRole(Role.ADMIN);
    }
}
