package com.example.Hotel_Booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Hotel_Booking.entity.Booking;
import com.example.Hotel_Booking.entity.Hotel;
import com.example.Hotel_Booking.entity.Room;
import com.example.Hotel_Booking.enums.RoomType;
import com.example.Hotel_Booking.repository.BookingRepository;
import com.example.Hotel_Booking.repository.HotelRepository;
import com.example.Hotel_Booking.repository.PaymentRepository;
import com.example.Hotel_Booking.repository.ReviewRepository;
import com.example.Hotel_Booking.repository.RoomRepository;

import jakarta.transaction.Transactional;

@Service
public class HotelService {

    private final HotelRepository hotelRepo;
    private final RoomRepository roomRepo;
    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final ReviewRepository reviewRepo;

    public HotelService(HotelRepository hotelRepo, RoomRepository roomRepo, BookingRepository bookingRepo,
            PaymentRepository paymentRepo, ReviewRepository reviewRepo) {
        this.hotelRepo = hotelRepo;
        this.roomRepo = roomRepo;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.reviewRepo = reviewRepo;
    }

    public Hotel addHotel(Hotel hotel) {
        applyHotelDefaults(hotel);
        @SuppressWarnings("null")
        Hotel savedHotel = hotelRepo.save(hotel);
        createOrUpdateDefaultRoom(savedHotel);
        return savedHotel;
    }

    public Hotel updateHotel(Long id, Hotel hotel) {
        Hotel existing = getHotel(id);
        existing.setName(hotel.getName());
        existing.setCity(hotel.getCity());
        existing.setAddress(hotel.getAddress());
        existing.setDescription(hotel.getDescription());
        existing.setHeading(hotel.getHeading());
        existing.setPricePerNight(hotel.getPricePerNight());
        existing.setRating(hotel.getRating());

        existing.setImages(hotel.getImages());

        existing.setRoomType(hotel.getRoomType());
        existing.setAvailable(hotel.isAvailable());
        existing.setAmenities(hotel.getAmenities());
        if (hotel.getLocation() != null && !hotel.getLocation().isBlank()) {
            existing.setLocation(hotel.getLocation());
        }
        applyHotelDefaults(existing);
        Hotel savedHotel = hotelRepo.save(existing);
        createOrUpdateDefaultRoom(savedHotel);
        return savedHotel;
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteHotel(Long id) {
        if (!hotelRepo.existsById(id)) {
            throw new RuntimeException("Hotel not found");
        }
        List<Booking> bookings = bookingRepo.findByHotelId(id);
        for (Booking booking : bookings) {
            reviewRepo.deleteByBookingId(booking.getId());
            paymentRepo.deleteByBookingId(booking.getId());
            bookingRepo.delete(booking);
        }
        hotelRepo.deleteById(id);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepo.findAll();
    }

    @SuppressWarnings("null")
    public Hotel getHotel(Long id) {
        return hotelRepo.findById(id).orElseThrow(() -> new RuntimeException("Hotel not found"));
    }

    public List<Hotel> searchByCity(String city) {
        if (city == null || city.isBlank()) {
            return hotelRepo.findAll();
        }
        return hotelRepo.findByCityContainingIgnoreCase(city.trim());
    }

    private void applyHotelDefaults(Hotel hotel) {
        if (hotel.getLocation() == null || hotel.getLocation().isBlank()) {
            hotel.setLocation((hotel.getCity() == null ? "" : hotel.getCity()) + " - "
                    + (hotel.getAddress() == null ? "" : hotel.getAddress()));
        }
        if (hotel.getRoomType() == null || hotel.getRoomType().isBlank()) {
            hotel.setRoomType("Luxury Room");
        }
    }

    private void createOrUpdateDefaultRoom(Hotel hotel) {
        Room room = roomRepo.findFirstByHotelIdOrderByIdAsc(hotel.getId()).orElseGet(Room::new);
        room.setHotel(hotel);
        room.setPrice(hotel.getPricePerNight());
        room.setAvailable(hotel.isAvailable());
        room.setType(toRoomType(hotel.getRoomType()));
        room.setPersonCount(room.getType().getMaxPersons());
        room.setTotalRooms(room.getTotalRooms() > 0 ? room.getTotalRooms() : 10);
        roomRepo.save(room);
    }

    private RoomType toRoomType(String roomType) {
        if (roomType == null) {
            return RoomType.DELUXE;
        }
        String normalized = roomType.toLowerCase();
        if (normalized.contains("single")) {
            return RoomType.SINGLE;
        }
        if (normalized.contains("double")) {
            return RoomType.DOUBLE;
        }
        if (normalized.contains("family")) {
            return RoomType.FAMILY;
        }
        return RoomType.DELUXE;
    }
}
