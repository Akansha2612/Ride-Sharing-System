package com.ridesharing.ride.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RideRequestDTO {

	 @NotNull(message = "Passenger ID is required")
	 @Positive(message = "Passenger ID must be positive")
	 private Long passengerId;
	 
	 @NotNull(message = "Pickup latitude is required")
	 private Double pickupLatitude;

	 @NotNull(message = "Pickup longitude is required")
	 private Double pickupLongitude;

	 @NotNull(message = "Destination latitude is required")
	 private Double destinationLatitude;

	 @NotNull(message = "Destination longitude is required")
	 private Double destinationLongitude;

	public RideRequestDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getPassengerId() {
		return passengerId;
	}

	public void setPassengerId(Long passengerId) {
		this.passengerId = passengerId;
	}

	public Double getPickupLatitude() {
		return pickupLatitude;
	}

	public void setPickupLatitude(Double pickupLatitude) {
		this.pickupLatitude = pickupLatitude;
	}

	public Double getPickupLongitude() {
		return pickupLongitude;
	}

	public void setPickupLongitude(Double pickupLongitude) {
		this.pickupLongitude = pickupLongitude;
	}

	public Double getDestinationLatitude() {
		return destinationLatitude;
	}

	public void setDestinationLatitude(Double destinationLatitude) {
		this.destinationLatitude = destinationLatitude;
	}

	public Double getDestinationLongitude() {
		return destinationLongitude;
	}

	public void setDestinationLongitude(Double destinationLongitude) {
		this.destinationLongitude = destinationLongitude;
	}
	 
	 
	 
	 
	 

}
