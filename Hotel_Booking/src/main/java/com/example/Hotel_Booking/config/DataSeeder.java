package com.example.Hotel_Booking.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.Hotel_Booking.entity.Hotel;
import com.example.Hotel_Booking.entity.Room;
import com.example.Hotel_Booking.entity.User;
import com.example.Hotel_Booking.enums.Role;
import com.example.Hotel_Booking.enums.RoomType;
import com.example.Hotel_Booking.repository.BookingRepository;
import com.example.Hotel_Booking.repository.HotelRepository;
import com.example.Hotel_Booking.repository.RoomRepository;
import com.example.Hotel_Booking.repository.UserRepository;

import jakarta.transaction.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(HotelRepository hotelRepository, RoomRepository roomRepository,
            BookingRepository bookingRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Seed test users first
        seedTestUsers();

        List<Hotel> hotels = hotelRepository.findAll();
        if (hotels.isEmpty()) {
            @SuppressWarnings("null")
            List<Hotel> savedHotels = hotelRepository.saveAll(List.of(
                    hotel("Taj Palace", "New Delhi", "Sardar Patel Marg, New Delhi",
                            "A premium city hotel with luxury rooms and fine dining.", 6500, 4.8, "Luxury Room"),
                    hotel("Oberoi", "Mumbai", "Nariman Point, Mumbai",
                            "Elegant sea-facing rooms in the heart of Mumbai.", 7200, 4.7, "Double Bed"),
                    hotel("Sea View Resort", "Goa", "Candolim Beach Road, Goa",
                            "Beach resort with pool access and sunset views.", 4800, 4.5, "Family Suite"),
                    hotel("Hilltop Stay", "Manali", "Mall Road, Manali",
                            "Mountain-view stay with warm rooms and breakfast.", 3500, 4.4, "Single Bed")));
            savedHotels.forEach(this::ensureRoom);
            return;
        }

        for (Hotel hotel : hotels) {
            boolean changed = false;
            if (hotel.getCity() == null || hotel.getCity().isBlank()) {
                applyDefaults(hotel);
                changed = true;
            }
            if (!hotel.isAvailable()) {
                hotel.setAvailable(true);
                changed = true;
            }
            if (hotel.getAmenities() == null || hotel.getAmenities().isEmpty()) {
                hotel.setAmenities(defaultAmenities());
                changed = true;
            }
            if (changed) {
                hotelRepository.save(hotel);
            }
            ensureRoom(hotel);
        }

        syncBookedRooms();
    }

    private void applyDefaults(Hotel hotel) {
        String name = hotel.getName() == null ? "" : hotel.getName().toLowerCase();
        if (name.contains("oberoi")) {
            fill(hotel, "Mumbai", "Nariman Point, Mumbai", "Elegant sea-facing rooms in the heart of Mumbai.", 7200,
                    4.7, "Double Bed");
        } else if (name.contains("sea")) {
            fill(hotel, "Goa", "Candolim Beach Road, Goa", "Beach resort with pool access and sunset views.", 4800, 4.5,
                    "Family Suite");
        } else if (name.contains("hill")) {
            fill(hotel, "Manali", "Mall Road, Manali", "Mountain-view stay with warm rooms and breakfast.", 3500, 4.4,
                    "Single Bed");
        } else {
            fill(hotel, "New Delhi", "Sardar Patel Marg, New Delhi",
                    "A premium city hotel with luxury rooms and fine dining.", 6500, 4.8, "Luxury Room");
        }
    }

    private Hotel hotel(String name, String city, String address, String description, double price, double rating,
            String roomType) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        fill(hotel, city, address, description, price, rating, roomType);
        hotel.setAvailable(true);
        hotel.setAmenities(defaultAmenities());
        hotel.setLocation(city + " - " + address);
        return hotel;
    }

    private List<String> defaultAmenities() {
        return new ArrayList<>(List.of("Free Wi-Fi", "Room Service", "Free Breakfast"));
    }

    private void fill(Hotel hotel, String city, String address, String description, double price, double rating,
            String roomType) {
        hotel.setCity(city);
        hotel.setAddress(address);
        hotel.setDescription(description);
        hotel.setPricePerNight(price);
        hotel.setRating(rating);
        hotel.setRoomType(roomType);
        hotel.setLocation(city + " - " + address);
    }

    private void ensureRoom(Hotel hotel) {
        java.util.Optional<Room> existingRoom = roomRepository.findFirstByHotelIdOrderByIdAsc(hotel.getId());
        if (existingRoom.isPresent()) {
            Room room = existingRoom.get();
            boolean changed = false;
            if (room.getTotalRooms() <= 0) {
                room.setTotalRooms(10);
                changed = true;
            }
            if (room.getPrice() <= 0 && hotel.getPricePerNight() > 0) {
                room.setPrice(hotel.getPricePerNight());
                changed = true;
            }
            if (room.getType() == null) {
                room.setType(toRoomType(hotel.getRoomType()));
                changed = true;
            }
            if (room.getPersonCount() <= 0 && room.getType() != null) {
                room.setPersonCount(room.getType().getMaxPersons());
                changed = true;
            }
            if (hotel.isAvailable() && !room.isAvailable()) {
                room.setAvailable(true);
                changed = true;
            }
            if (changed) {
                roomRepository.save(room);
            }
            return;
        }
        Room room = new Room();
        room.setHotel(hotel);
        room.setPrice(hotel.getPricePerNight());
        room.setAvailable(hotel.isAvailable());
        room.setType(toRoomType(hotel.getRoomType()));
        room.setPersonCount(room.getType().getMaxPersons());
        room.setTotalRooms(10);
        room.setBookedRooms(0);
        roomRepository.save(room);
    }

    private void syncBookedRooms() {
        for (Room room : roomRepository.findAll()) {
            Long activeBookedRooms = bookingRepository.countActiveBookedRoomsByRoomId(room.getId());
            int bookedRooms = activeBookedRooms == null ? 0 : activeBookedRooms.intValue();
            boolean changed = room.getBookedRooms() != bookedRooms
                    || room.isAvailable() != (bookedRooms < room.getTotalRooms());
            if (changed) {
                room.setBookedRooms(bookedRooms);
                room.setAvailable(bookedRooms < room.getTotalRooms());
                roomRepository.save(room);
            }
        }
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
        return RoomType.DELUXE;
    }

    private void seedTestUsers() {
        // Check if admin@pavan.com already exists
        java.util.Optional<User> existingUser = userRepository.findByEmail("admin@pavan.com");
        if (existingUser.isPresent()) {
            System.out.println("✓ Custom Admin already exists");
            return;
        }

        try {
            // Create test user
            if (userRepository.findByEmail("user@test.com").isEmpty()) {
                User testUser = new User();
                testUser.setName("Test User");
                testUser.setEmail("user@test.com");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setRole(Role.USER);
                testUser.setMobileNo("9876543210");
                testUser.setGender("Not Specified");
                userRepository.save(testUser);
                System.out.println("✓ Created test user: user@test.com");
            }

            // Create custom admin
            User testAdmin = new User();
            testAdmin.setName("Pavan Yadav");
            testAdmin.setEmail("admin@pavan.com");
            testAdmin.setPassword(passwordEncoder.encode("admin@123"));
            testAdmin.setRole(Role.ADMIN);
            testAdmin.setMobileNo("9876543211");
            testAdmin.setGender("Male");
            User savedAdmin = userRepository.save(testAdmin);
            System.out.println("✓ Created test admin: " + savedAdmin.getEmail());
        } catch (Exception e) {
            System.err.println("✗ Error seeding test users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
