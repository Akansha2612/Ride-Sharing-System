package com.ridesharing.passenger.exception;

public class DuplicatePassengerException extends RuntimeException {

    public DuplicatePassengerException(String email) {
        super("Passenger already registered with email: " + email);
    }
}
