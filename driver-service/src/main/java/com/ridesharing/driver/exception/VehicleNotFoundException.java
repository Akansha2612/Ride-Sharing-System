package com.ridesharing.driver.exception;

public class VehicleNotFoundException extends RuntimeException {
	 public VehicleNotFoundException(Long id) {
	        super("Vehicle not found with ID: " + id);
	    }
}
