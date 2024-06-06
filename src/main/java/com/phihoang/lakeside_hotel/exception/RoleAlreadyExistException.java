package com.phihoang.lakeside_hotel.exception;

public class RoleAlreadyExistException extends RuntimeException{
    public RoleAlreadyExistException(String message) {
        super(message);
    }
}
