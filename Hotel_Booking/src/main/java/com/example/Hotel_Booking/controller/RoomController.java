package com.example.Hotel_Booking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Hotel_Booking.dto.ResponseDTO;
import com.example.Hotel_Booking.entity.Room;
import com.example.Hotel_Booking.service.RoomService;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN → POST /api/rooms/{hotelId}  →  add room to hotel
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/{hotelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO addRoom(@PathVariable Long hotelId,
                               @RequestBody Room room) {
        return new ResponseDTO(true, "Room added successfully",
                roomService.addRoom(hotelId, room));
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN → PUT /api/rooms/{roomId}  →  update room
    // ─────────────────────────────────────────────────────────────
    @PutMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO updateRoom(@PathVariable Long roomId,
                                  @RequestBody Room room) {
        return new ResponseDTO(true, "Room updated successfully",
                roomService.updateRoom(roomId, room));
    }

    // ─────────────────────────────────────────────────────────────
    // ADMIN → DELETE /api/rooms/{roomId}  →  delete room
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseDTO(true, "Room deleted successfully", null);
    }

    // ─────────────────────────────────────────────────────────────
    // Public → GET /api/rooms/{hotelId}  →  all rooms for a hotel
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/{hotelId}")
    public ResponseDTO getRoomsByHotel(@PathVariable Long hotelId) {
        return new ResponseDTO(true, "Rooms fetched",
                roomService.getRoomsByHotel(hotelId));
    }

    // ─────────────────────────────────────────────────────────────
    // Public → GET /api/rooms/available?hotelId=1&checkIn=...&checkOut=...
    // Returns only rooms NOT booked for the requested dates
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/available")
    public ResponseDTO getAvailableRooms(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        List<Room> rooms = roomService.getAvailableRooms(hotelId, checkIn, checkOut);
        return new ResponseDTO(true, "Available rooms fetched", rooms);
    }
}