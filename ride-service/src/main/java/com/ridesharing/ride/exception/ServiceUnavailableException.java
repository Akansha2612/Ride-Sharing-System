package com.ridesharing.ride.exception;

public class ServiceUnavailableException extends RuntimeException  {
	public ServiceUnavailableException(String message) {
        super(message);
    }
}
