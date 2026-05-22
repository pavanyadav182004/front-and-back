package com.example.Hotel_Booking.enums;

public enum RoomType {
    SINGLE(1),
    DOUBLE(2),
    DELUXE(4),
    FAMILY(6);

    private final int maxPersons;

    RoomType(int maxPersons) {
        this.maxPersons = maxPersons;
    }

    public int getMaxPersons() {
        return maxPersons;
    }
}
