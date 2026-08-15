package com.example.Hotel_Booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Hotel_Booking.entity.Offer;
import com.example.Hotel_Booking.repository.OfferRepository;

@Service
public class OfferService {

    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    public Offer getOffer(Long id) {
        return offerRepository.findById(id).orElseThrow(() -> new RuntimeException("Offer not found"));
    }

    public Offer addOffer(Offer offer) {
        return offerRepository.save(offer);
    }

    public Offer updateOffer(Long id, Offer offer) {
        Offer existing = getOffer(id);
        existing.setTitle(offer.getTitle());
        existing.setDescription(offer.getDescription());
        existing.setPriceOff(offer.getPriceOff());
        existing.setExpiryDate(offer.getExpiryDate());
        
        // Only update image if a new one is provided (optional behavior, but good for base64)
        if (offer.getImage() != null && !offer.getImage().isEmpty()) {
            existing.setImage(offer.getImage());
        }

        return offerRepository.save(existing);
    }

    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offer not found");
        }
        offerRepository.deleteById(id);
    }
}
