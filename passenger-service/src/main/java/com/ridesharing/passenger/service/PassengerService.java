package com.ridesharing.passenger.service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ridesharing.passenger.Entity.Passenger;
import com.ridesharing.passenger.Entity.Passenger.AccountStatus;

import com.ridesharing.passenger.dto.PassengerRequestDTO;
import com.ridesharing.passenger.dto.PassengerResponseDTO;
import com.ridesharing.passenger.exception.DuplicatePassengerException;
import com.ridesharing.passenger.exception.InvalidPassengerRequestException;
import com.ridesharing.passenger.exception.PassengerNotFoundException;
import com.ridesharing.passenger.repository.PassengerRepository;

@Service
public class PassengerService {

	 private final PassengerRepository repository;

	    public PassengerService(PassengerRepository repository) {
	        this.repository = repository;
	    }

	    public PassengerResponseDTO  registerPassenger( PassengerRequestDTO request) {
	    	String email = normalizeEmail(request.getEmail());

	        if (repository.existsByEmail(email)) {
	            throw new DuplicatePassengerException(email);
	        }

	        Passenger passenger = new Passenger();

	        copyToEntity(request, passenger);

	        passenger.setAccountStatus(AccountStatus.ACTIVE);
	        Passenger savedPassenger = repository.save(passenger);

	        return convertToDTO(savedPassenger);
	    }

	    public Page<PassengerResponseDTO> getAllPassengers( int page, int size) {
	    	 if (page < 0 || size < 1 || size > 100) {
	             throw new InvalidPassengerRequestException(
	                 "Page must be >= 0 and size must be between 1 and 100"
	             );
	         }

	         PageRequest pageable = PageRequest.of(
	             page,
	             size,
	             Sort.by("id").ascending()
	         );

	         return repository.findAll(pageable)
	                 .map(this::convertToDTO);
	    }
	    
	 // Get Passenger By ID
	    public PassengerResponseDTO  getPassengerById(Long id) {
	    	Passenger passenger = findPassenger(id);

	        return convertToDTO(passenger);
	    }

	    // Update Passenger
	    public PassengerResponseDTO  updatePassenger( Long id, PassengerRequestDTO request) {
	        Passenger passenger = findPassenger(id);

	        String email = normalizeEmail(request.getEmail());

	        if (repository.existsByEmailAndIdNot(email, id)) {
	            throw new DuplicatePassengerException(email);
	        }

	        copyToEntity(request, passenger);

	        Passenger updatedPassenger = repository.save(passenger);

	        return convertToDTO(updatedPassenger);
	    }

	    // Delete Passenger
	    public void deletePassenger(Long id) {
	    	 Passenger passenger = findPassenger(id);

	         repository.delete(passenger);
	    }
	    
	    // Helper: Find Passenger Entity
	    private Passenger findPassenger(Long id) {

	        return repository.findById(id)
	                .orElseThrow(() ->
	                    new PassengerNotFoundException(id)
	                );
	    }
	    
	 // 7. Update Location Only
	    public PassengerResponseDTO updatePassengerLocation(
	            Long id, Double latitude, Double longitude) {

	        Passenger passenger = findPassenger(id);

	        validateCoordinates(latitude, longitude);

	        passenger.setLatitude(latitude);
	        passenger.setLongitude(longitude);
	        passenger.setLocationUpdatedAt(Instant.now());

	        return convertToDTO(
	        		repository.save(passenger)
	        );
	    }
	    
	 // 8. Update Account Status
	    // Development only. Requires admin authorization in production.
	    public PassengerResponseDTO updatePassengerStatus(
	            Long id, AccountStatus status) {

	        Passenger passenger = findPassenger(id);

	        if (status == null) {
	            throw new InvalidPassengerRequestException(
	                "Account status is required"
	            );
	        }

	        passenger.setAccountStatus(status);

	        return convertToDTO(
	        		repository.save(passenger)
	        );
	    }
	    
	    // Helper: Normalize Email
	    private String normalizeEmail(String email) {
	        return email.trim().toLowerCase(Locale.ROOT);
	    }
	    
	 // Helper: Validate Coordinates
	    private void validateCoordinates(
	            Double latitude, Double longitude) {

	        if (latitude == null || longitude == null ||
	                !Double.isFinite(latitude) ||
	                !Double.isFinite(longitude) ||
	                latitude < -90 || latitude > 90 ||
	                longitude < -180 || longitude > 180) {

	            throw new InvalidPassengerRequestException(
	                "Invalid latitude or longitude"
	            );
	        }
	    }

	    
	    
	    // Helper: Copy Request DTO To Entity
	    private void copyToEntity(
	            PassengerRequestDTO request,
	            Passenger passenger) {

	        passenger.setName(request.getName().trim());
	        passenger.setEmail(normalizeEmail(request.getEmail()));
	        passenger.setPhone(request.getPhone().trim());
	        
	        Double latitude = request.getLatitude();
	        Double longitude = request.getLongitude();
	        
	        // Keep existing location if both fields are omitted.
	        if (latitude != null || longitude != null) {

	            validateCoordinates(latitude, longitude);

	            passenger.setLatitude(latitude);
	            passenger.setLongitude(longitude);
	            passenger.setLocationUpdatedAt(Instant.now());
	        }
	    }
	    
	    // Helper Method: Entity to Response DTO
	    private PassengerResponseDTO convertToDTO(
	            Passenger passenger) {

	        PassengerResponseDTO response =
	                new PassengerResponseDTO();

	        response.setId(passenger.getId());
	        response.setName(passenger.getName());
	        response.setEmail(passenger.getEmail());
	        response.setPhone(passenger.getPhone());

	        response.setLatitude(passenger.getLatitude());
	        response.setLongitude(passenger.getLongitude());
	        response.setLocationUpdatedAt(
	            passenger.getLocationUpdatedAt()
	        );
	        response.setAccountStatus(passenger.getAccountStatus());

	        
	        return response;
	    }
}
