package com.example.Hotel_Booking.controller;

import org.springframework.web.bind.annotation.*;
import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.entity.User;
import com.example.Hotel_Booking.repository.UserRepository;
import com.example.Hotel_Booking.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/profile-image")
    public ResponseDTO updateProfileImage(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        if (email == null) {
            throw new CustomException("User not authenticated");
        }

        String imageUrl = payload.get("imageUrl");
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new CustomException("Image URL/Data is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        user.setImageUrl(imageUrl);
        @SuppressWarnings("null")
        User image = user;
        userRepository.save(image);

        return new ResponseDTO(true, "Profile image updated successfully", imageUrl);
    }

    @PutMapping("/profile")
    public ResponseDTO updateProfile(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        if (email == null) {
            throw new CustomException("User not authenticated");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        if (payload.containsKey("name")) {
            user.setName(payload.get("name"));
        }
        if (payload.containsKey("mobileNo")) {
            user.setMobileNo(payload.get("mobileNo"));
        }
        if (payload.containsKey("address")) {
            user.setAddress(payload.get("address"));
        }
        if (payload.containsKey("gender")) {
            user.setGender(payload.get("gender"));
        }

        @SuppressWarnings("null")
        User updatedUser = user;
        @SuppressWarnings({ "null", "unused" })
        Object result = userRepository.save(updatedUser);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("imageUrl", user.getImageUrl());
        data.put("mobileNo", user.getMobileNo());
        data.put("address", user.getAddress());
        data.put("gender", user.getGender());

        return new ResponseDTO(true, "Profile updated successfully", data);
    }
}
