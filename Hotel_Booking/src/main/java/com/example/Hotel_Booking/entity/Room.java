package com.example.Hotel_Booking.entity;

import com.example.Hotel_Booking.enums.RoomType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double price;

    private boolean available = true;

    private int personCount;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    // 🔥 IMPORTANT (NEW FIELDS — DB sync)
    @Column(nullable = false)
    private int totalRooms;

    @Column(nullable = false)
    private int bookedRooms = 0;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonIgnore
    private Hotel hotel;

    // ─────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────
    public Room() {
    }

    public Room(Long id, double price, boolean available, int personCount,
                RoomType type, int totalRooms, int bookedRooms, Hotel hotel) {
        this.id = id;
        this.price = price;
        this.available = available;
        this.personCount = personCount;
        this.type = type;
        this.totalRooms = totalRooms;
        this.bookedRooms = bookedRooms;
        this.hotel = hotel;
    }

    // ─────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getPersonCount() {
        return personCount;
    }

    public void setPersonCount(int personCount) {
        this.personCount = personCount;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getBookedRooms() {
        return bookedRooms;
    }

    public void setBookedRooms(int bookedRooms) {
        this.bookedRooms = bookedRooms;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    // ─────────────────────────────────────
    // 🔥 Availability Logic (important)
    // ─────────────────────────────────────
    public boolean isRoomAvailable() {
        return bookedRooms < totalRooms;
    }
}