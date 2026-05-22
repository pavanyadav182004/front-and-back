package com.example.Hotel_Booking.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.entity.ContactMessage;
import com.example.Hotel_Booking.repository.ContactMessageRepository;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {
    private final ContactMessageRepository contactMessageRepository;

    public ContactController(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    @PostMapping
    public ResponseDTO sendMessage(@RequestBody ContactMessage contactMessage) {
        if (contactMessage.getEmail() == null || contactMessage.getEmail().isBlank()) {
            return new ResponseDTO(false, "Email is required", null);
        }
        if (contactMessage.getSubject() == null || contactMessage.getSubject().isBlank()) {
            contactMessage.setSubject("Newsletter subscription");
        }
        return new ResponseDTO(true, "Message received successfully", contactMessageRepository.save(contactMessage));
    }

    @GetMapping
    public ResponseDTO getMessages() {
        return new ResponseDTO(true, "Messages fetched successfully", contactMessageRepository.findAll());
    }
}
