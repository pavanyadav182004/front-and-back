package com.example.Hotel_Booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Hotel_Booking.entity.Hotel;
import com.example.Hotel_Booking.entity.Room;
import com.example.Hotel_Booking.exception.CustomException;
import com.example.Hotel_Booking.repository.HotelRepository;
import com.example.Hotel_Booking.repository.RoomRepository;

@Service
public class RoomService {

    private final RoomRepository roomRepo;
    private final HotelRepository hotelRepo;

    public RoomService(RoomRepository roomRepo, HotelRepository hotelRepo) {
        this.roomRepo  = roomRepo;
        this.hotelRepo = hotelRepo;
    }

    /** ADMIN: add a room to a hotel */
    public Room addRoom(Long hotelId, Room room) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new CustomException("Hotel not found"));
        room.setHotel(hotel);
        return roomRepo.save(room);
    }

    /** ADMIN: update a room */
    public Room updateRoom(Long roomId, Room roomDetails) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new CustomException("Room not found"));
        room.setPrice(roomDetails.getPrice());
        room.setType(roomDetails.getType());
        room.setTotalRooms(roomDetails.getTotalRooms());
        room.setPersonCount(roomDetails.getPersonCount());
        room.setAvailable(roomDetails.isAvailable());
        return roomRepo.save(room);
    }

    /** ADMIN: delete a room */
    public void deleteRoom(Long roomId) {
        if (!roomRepo.existsById(roomId)) {
            throw new CustomException("Room not found");
        }
        roomRepo.deleteById(roomId);
    }

    /** Public: get all rooms for a hotel */
    public List<Room> getRoomsByHotel(Long hotelId) {
        ensureDefaultRoom(hotelId);
        return roomRepo.findByHotelId(hotelId);
    }

    /** Public: get available rooms for a hotel within a date range */
    public List<Room> getAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new CustomException("Invalid date range: checkOut must be after checkIn");
        }

        // 🔥 Check if the hotel itself is available
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new CustomException("Hotel not found"));
        if (!hotel.isAvailable()) {
            throw new CustomException("This hotel is currently not available for booking.");
        }

        ensureDefaultRoom(hotelId);
        return roomRepo.findAvailableRooms(hotelId, checkIn, checkOut);
    }

    private void ensureDefaultRoom(Long hotelId) {
        Hotel hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new CustomException("Hotel not found"));

        var existingRoom = roomRepo.findFirstByHotelIdOrderByIdAsc(hotelId);
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
                roomRepo.save(room);
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
        roomRepo.save(room);
    }

    private com.example.Hotel_Booking.enums.RoomType toRoomType(String roomType) {
        if (roomType == null) {
            return com.example.Hotel_Booking.enums.RoomType.DELUXE;
        }
        String normalized = roomType.toLowerCase();
        if (normalized.contains("single")) {
            return com.example.Hotel_Booking.enums.RoomType.SINGLE;
        }
        if (normalized.contains("double")) {
            return com.example.Hotel_Booking.enums.RoomType.DOUBLE;
        }
        return com.example.Hotel_Booking.enums.RoomType.DELUXE;
    }
}
