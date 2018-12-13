package com.campsite.exception;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException(String s) {
        super(s);
    }
}
