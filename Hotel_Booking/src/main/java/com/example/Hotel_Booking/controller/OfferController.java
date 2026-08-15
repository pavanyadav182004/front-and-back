package com.example.Hotel_Booking.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.entity.Offer;
import com.example.Hotel_Booking.service.OfferService;

@RestController
@RequestMapping("/api/offers")
@CrossOrigin(origins = "*")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public ResponseDTO getAllOffers() {
        return new ResponseDTO(true, "Offers fetched successfully", offerService.getAllOffers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO addOffer(@RequestBody Offer offer) {
        return new ResponseDTO(true, "Offer added successfully", offerService.addOffer(offer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO updateOffer(@PathVariable Long id, @RequestBody Offer offer) {
        return new ResponseDTO(true, "Offer updated successfully", offerService.updateOffer(id, offer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return new ResponseDTO(true, "Offer deleted successfully", null);
    }
}
