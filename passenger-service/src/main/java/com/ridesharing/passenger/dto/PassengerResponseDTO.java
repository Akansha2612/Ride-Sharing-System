
package com.ridesharing.passenger.dto;

import java.time.Instant;

import com.ridesharing.passenger.Entity.Passenger.AccountStatus;


public class PassengerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;

    private Double latitude;
    private Double longitude;
    private Instant locationUpdatedAt;
    private AccountStatus  accountStatus;

    // Default Constructor
    public PassengerResponseDTO() {
    }

    // Parameterized Constructor
    public PassengerResponseDTO(
            Long id,
            String name,
            String email,
            String phone) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters

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

	
    
}
