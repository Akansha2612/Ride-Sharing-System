package com.ridesharing.driver.dto;

import java.time.Instant;

import com.ridesharing.driver.entity.Driver.AccountStatus;
import com.ridesharing.driver.entity.Driver.AvailabilityStatus;
import com.ridesharing.driver.entity.Driver.VerificationStatus;

public class DriverResponseDTO {
	 private Long id;
	    private String name;
	    private String email;
	    private String phone;

	    private Double latitude;
	    private Double longitude;
	    private Instant locationUpdatedAt;

	    private AccountStatus accountStatus;
	    private AvailabilityStatus availabilityStatus;
	    private VerificationStatus verificationStatus;

	    private Double distanceKm;

		public DriverResponseDTO() {
			super();
			// TODO Auto-generated constructor stub
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public Double getLatitude() {
			return latitude;
		}

		public void setLatitude(Double latitude) {
			this.latitude = latitude;
		}

		public Double getLongitude() {
			return longitude;
		}

		public void setLongitude(Double longitude) {
			this.longitude = longitude;
		}

		public Instant getLocationUpdatedAt() {
			return locationUpdatedAt;
		}

		public void setLocationUpdatedAt(Instant locationUpdatedAt) {
			this.locationUpdatedAt = locationUpdatedAt;
		}

		public AccountStatus getAccountStatus() {
			return accountStatus;
		}

		public void setAccountStatus(AccountStatus accountStatus) {
			this.accountStatus = accountStatus;
		}

		public AvailabilityStatus getAvailabilityStatus() {
			return availabilityStatus;
		}

		public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
			this.availabilityStatus = availabilityStatus;
		}

		public VerificationStatus getVerificationStatus() {
			return verificationStatus;
		}

		public void setVerificationStatus(VerificationStatus verificationStatus) {
			this.verificationStatus = verificationStatus;
		}

		public Double getDistanceKm() {
			return distanceKm;
		}

		public void setDistanceKm(Double distanceKm) {
			this.distanceKm = distanceKm;
		}

	    
}
