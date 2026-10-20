package com.ridesharing.passenger.exception;

public class PassengerNotFoundException extends RuntimeException {

    public PassengerNotFoundException(Long id) {
        super("Passenger not found with ID: " + id);
    }
}
