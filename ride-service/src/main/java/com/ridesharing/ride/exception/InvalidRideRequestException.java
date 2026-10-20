package com.ridesharing.ride.exception;

public class InvalidRideRequestException extends RuntimeException {

	public InvalidRideRequestException(String message) {
        super(message);
    }
}
