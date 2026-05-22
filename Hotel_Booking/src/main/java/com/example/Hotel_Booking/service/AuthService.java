package com.example.Hotel_Booking.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.dto.UserDTO;
import com.example.Hotel_Booking.entity.User;
import com.example.Hotel_Booking.enums.Role;
import com.example.Hotel_Booking.repository.UserRepository;
import com.example.Hotel_Booking.securityJWT.JwtUtil;

@Service
public class AuthService {

    private static final long OTP_VALIDITY_MS = 5 * 60 * 1000;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final Map<String, OtpEntry> resetOtps = new ConcurrentHashMap<>();
    private final Map<String, OtpEntry> registerOtps = new ConcurrentHashMap<>();
    private final Map<String, VerifiedRegistration> verifiedRegistrations = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public ResponseDTO sendRegistrationOtp(String email, String requestedRole) {
        String normalizedEmail = normalizeEmail(email);
        if (!isValidEmail(normalizedEmail)) {
            return new ResponseDTO(false, "Valid email is required", null);
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return new ResponseDTO(false, "Email already registered", null);
        }

        Role role = parseRequestedRole(requestedRole);
        if (role == Role.ADMIN && userRepository.existsByRole(Role.ADMIN)) {
            return new ResponseDTO(false, "Admin already exists. Only user registration is allowed.", null);
        }

        String otp = generateOtp();
        registerOtps.put(registrationKey(normalizedEmail, role), new OtpEntry(otp, System.currentTimeMillis() + OTP_VALIDITY_MS));
        emailService.sendOtp(normalizedEmail, "Verify your Hotel Booking email", otp);

        Map<String, Object> data = new HashMap<>();
        data.put("expiresInMinutes", 5);
        return new ResponseDTO(true, "OTP sent to your email", data);
    }

    public ResponseDTO verifyRegistrationOtp(String email, String otp, String requestedRole) {
        String normalizedEmail = normalizeEmail(email);
        Role role = parseRequestedRole(requestedRole);
        ResponseDTO otpCheck = validateOtp(registerOtps, registrationKey(normalizedEmail, role), otp);
        if (!otpCheck.isSuccess()) {
            return otpCheck;
        }

        String token = UUID.randomUUID().toString();
        verifiedRegistrations.put(token, new VerifiedRegistration(normalizedEmail, role, System.currentTimeMillis() + OTP_VALIDITY_MS));
        registerOtps.remove(registrationKey(normalizedEmail, role));

        Map<String, Object> data = new HashMap<>();
        data.put("verificationToken", token);
        data.put("expiresInMinutes", 5);
        return new ResponseDTO(true, "Email verified successfully", data);
    }

    public ResponseDTO registerUser(UserDTO dto) {
        ResponseDTO tokenCheck = validateRegistrationToken(dto, Role.USER);
        if (!tokenCheck.isSuccess()) {
            return tokenCheck;
        }

        String email = normalizeEmail(dto.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            return new ResponseDTO(false, "Email already registered", null);
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setImageUrl(dto.getImageUrl());

        User saved = userRepository.save(user);
        verifiedRegistrations.remove(dto.getVerificationToken());

        return new ResponseDTO(true, "User registered successfully", publicUser(saved));
    }

    public ResponseDTO registerAdmin(UserDTO dto) {
        ResponseDTO tokenCheck = validateRegistrationToken(dto, Role.ADMIN);
        if (!tokenCheck.isSuccess()) {
            return tokenCheck;
        }

        if (userRepository.existsByRole(Role.ADMIN)) {
            return new ResponseDTO(false, "Admin already exists", null);
        }

        String email = normalizeEmail(dto.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            return new ResponseDTO(false, "Email already registered", null);
        }

        User admin = new User();
        admin.setName(dto.getName().trim());
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setImageUrl(dto.getImageUrl());

        User saved = userRepository.save(admin);
        verifiedRegistrations.remove(dto.getVerificationToken());

        return new ResponseDTO(true, "Admin registered successfully", publicUser(saved));
    }

    public ResponseDTO login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(normalizeEmail(email));

        if (optionalUser.isEmpty()) {
            return new ResponseDTO(false, "User not found", null);
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new ResponseDTO(false, "Invalid password", null);
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", publicUser(user));

        return new ResponseDTO(true, "Login successful", data);
    }

    public ResponseDTO forgotPassword(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (!isValidEmail(normalizedEmail)) {
            return new ResponseDTO(false, "Valid email is required", null);
        }

        Optional<User> optionalUser = userRepository.findByEmail(normalizedEmail);
        if (optionalUser.isEmpty()) {
            return new ResponseDTO(false, "User not found", null);
        }

        String otp = generateOtp();
        resetOtps.put(normalizedEmail, new OtpEntry(otp, System.currentTimeMillis() + OTP_VALIDITY_MS));
        emailService.sendOtp(normalizedEmail, "Reset your Hotel Booking password", otp);

        Map<String, Object> data = new HashMap<>();
        data.put("expiresInMinutes", 5);
        return new ResponseDTO(true, "OTP sent to your registered email", data);
    }

    public ResponseDTO resetPassword(String email, String otp, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        if (!isValidEmail(normalizedEmail)) {
            return new ResponseDTO(false, "Valid email is required", null);
        }
        
        ResponseDTO complexityCheck = validatePasswordComplexity(newPassword);
        if (!complexityCheck.isSuccess()) {
            return complexityCheck;
        }

        ResponseDTO otpCheck = validateOtp(resetOtps, normalizedEmail, otp);
        if (!otpCheck.isSuccess()) {
            return otpCheck;
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetOtps.remove(normalizedEmail);

        return new ResponseDTO(true, "Password reset successfully", null);
    }

    private ResponseDTO validateRegistrationToken(UserDTO dto, Role expectedRole) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            return new ResponseDTO(false, "Name is required", null);
        }
        
        ResponseDTO complexityCheck = validatePasswordComplexity(dto.getPassword());
        if (!complexityCheck.isSuccess()) {
            return complexityCheck;
        }

        String email = normalizeEmail(dto.getEmail());
        if (!isValidEmail(email)) {
            return new ResponseDTO(false, "Valid email is required", null);
        }
        if (dto.getVerificationToken() == null || dto.getVerificationToken().isBlank()) {
            return new ResponseDTO(false, "Please verify email OTP before registration", null);
        }

        VerifiedRegistration verified = verifiedRegistrations.get(dto.getVerificationToken());
        if (verified == null) {
            return new ResponseDTO(false, "Email verification is required", null);
        }
        if (verified.expiresAt() < System.currentTimeMillis()) {
            verifiedRegistrations.remove(dto.getVerificationToken());
            return new ResponseDTO(false, "Email verification expired. Please request OTP again.", null);
        }
        if (!verified.email().equals(email) || verified.role() != expectedRole) {
            return new ResponseDTO(false, "Email verification does not match this registration", null);
        }

        return new ResponseDTO(true, "OK", null);
    }

    private ResponseDTO validateOtp(Map<String, OtpEntry> store, String key, String otp) {
        if (otp == null || otp.isBlank()) {
            return new ResponseDTO(false, "OTP is required", null);
        }

        OtpEntry entry = store.get(key);
        if (entry == null || !entry.otp().equals(otp.trim())) {
            return new ResponseDTO(false, "Invalid OTP", null);
        }
        if (entry.expiresAt() < System.currentTimeMillis()) {
            store.remove(key);
            return new ResponseDTO(false, "OTP expired", null);
        }

        return new ResponseDTO(true, "OK", null);
    }

    private Role parseRequestedRole(String requestedRole) {
        return "admin".equalsIgnoreCase(requestedRole) ? Role.ADMIN : Role.USER;
    }

    private String registrationKey(String email, Role role) {
        return role.name() + ":" + email;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private ResponseDTO validatePasswordComplexity(String password) {
        if (password == null || password.length() < 6) {
            return new ResponseDTO(false, "Password must be at least 6 characters", null);
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            return new ResponseDTO(false, "Password must contain at least one letter", null);
        }
        if (!password.matches(".*[0-9].*")) {
            return new ResponseDTO(false, "Password must contain at least one number", null);
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return new ResponseDTO(false, "Password must contain at least one special character", null);
        }
        if (password.equalsIgnoreCase("123456") || password.equalsIgnoreCase("654321") || 
            password.equalsIgnoreCase("password") || password.equalsIgnoreCase("admin123")) {
            return new ResponseDTO(false, "Too simple password, please choose a stronger one", null);
        }
        return new ResponseDTO(true, "OK", null);
    }

    private Map<String, Object> publicUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("imageUrl", user.getImageUrl());
        data.put("mobileNo", user.getMobileNo());
        data.put("address", user.getAddress());
        data.put("gender", user.getGender());
        return data;
    }

    private record OtpEntry(String otp, long expiresAt) {}

    private record VerifiedRegistration(String email, Role role, long expiresAt) {}
}
