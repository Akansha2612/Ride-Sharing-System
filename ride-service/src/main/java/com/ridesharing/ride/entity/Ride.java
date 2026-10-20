package com.ridesharing.ride.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "rides")
public class Ride {

	 public enum RideStatus {
	        REQUESTED,
	        SEARCHING,
	        ASSIGNED,
	        ACCEPTED,
	        IN_PROGRESS,
	        COMPLETED,
	        CANCELLED
	    }

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(nullable = false)
	    private Long passengerId;

	    private Long driverId;

	    @Column(nullable = false)
	    private Double pickupLatitude;

	    @Column(nullable = false)
	    private Double pickupLongitude;

	    @Column(nullable = false)
	    private Double destinationLatitude;

	    @Column(nullable = false)
	    private Double destinationLongitude;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private RideStatus rideStatus = RideStatus.REQUESTED;

	    @Column(nullable = false, updatable = false)
	    private Instant requestedAt;

	    private Instant acceptedAt;
	    private Instant startedAt;
	    private Instant completedAt;
	    private Instant cancelledAt;

	    public Ride() {
	    }

	    @PrePersist
	    public void onCreate() {
	        if (requestedAt == null) {
	            requestedAt = Instant.now();
	        }

	        if (rideStatus == null) {
	            rideStatus = RideStatus.REQUESTED;
	        }
	    }

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getPassengerId() {
			return passengerId;
		}

		public void setPassengerId(Long passengerId) {
			this.passengerId = passengerId;
		}

		public Long getDriverId() {
			return driverId;
		}

		public void setDriverId(Long driverId) {
			this.driverId = driverId;
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

		public RideStatus getRideStatus() {
			return rideStatus;
		}

		public void setRideStatus(RideStatus rideStatus) {
			this.rideStatus = rideStatus;
		}

		public Instant getRequestedAt() {
			return requestedAt;
		}

		public void setRequestedAt(Instant requestedAt) {
			this.requestedAt = requestedAt;
		}

		public Instant getAcceptedAt() {
			return acceptedAt;
		}

		public void setAcceptedAt(Instant acceptedAt) {
			this.acceptedAt = acceptedAt;
		}

		public Instant getStartedAt() {
			return startedAt;
		}

		public void setStartedAt(Instant startedAt) {
			this.startedAt = startedAt;
		}

		public Instant getCompletedAt() {
			return completedAt;
		}

		public void setCompletedAt(Instant completedAt) {
			this.completedAt = completedAt;
		}

		public Instant getCancelledAt() {
			return cancelledAt;
		}

		public void setCancelledAt(Instant cancelledAt) {
			this.cancelledAt = cancelledAt;
		}
	    
	    
	    
}
