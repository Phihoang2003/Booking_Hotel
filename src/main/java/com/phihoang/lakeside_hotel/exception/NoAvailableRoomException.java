package com.phihoang.lakeside_hotel.exception;

public class NoAvailableRoomException extends RuntimeException {
    public NoAvailableRoomException(String message) {
        super(message);
    }
}
