package com.ridesharing.ride.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.naming.ServiceUnavailableException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ridesharing.ride.dto.RideResponseDTO;
import com.ridesharing.ride.dto.RideRequestDTO;
import com.ridesharing.ride.entity.Ride;
import com.ridesharing.ride.entity.Ride.RideStatus;
import com.ridesharing.ride.exception.InvalidRideRequestException;
import com.ridesharing.ride.exception.RideConflictException;
import com.ridesharing.ride.exception.RideNotFoundException;
import com.ridesharing.ride.repository.RideRepository;

import jakarta.validation.Valid;

@Service
public class RideService {

	private final RideRepository rideRepository;
	private final PassengerClient passengerClient;
	private final DriverClient driverClient;
	
	public RideService(
            RideRepository rideRepository,
            PassengerClient passengerClient,
            DriverClient driverClient) {

        this.rideRepository = rideRepository;
        this.passengerClient = passengerClient;
        this.driverClient = driverClient;
    }
	 
	public RideResponseDTO requestRide(@Valid RideRequestDTO request) throws ServiceUnavailableException {
		// TODO Auto-generated method stub
		if (request == null) {
            throw new InvalidRideRequestException(
                "Ride request is required"
            );
        }
		 if (request.getPassengerId() == null || request.getPassengerId() <= 0) {

	            throw new InvalidRideRequestException(
	                "Valid passenger ID is required"
	            );
	        }
		 
		 validateCoordinates(request.getPickupLatitude(),request.getPickupLongitude());

		 validateCoordinates(request.getDestinationLatitude(),request.getDestinationLongitude());
		        
		 if (request.getPickupLatitude().equals(
	                request.getDestinationLatitude()) &&
	            request.getPickupLongitude().equals(
	                request.getDestinationLongitude())) {

	            throw new InvalidRideRequestException(
	                "Pickup and destination cannot be the same"
	            );
	        }
		 
		 // Check passenger in Passenger Service
	        validatePassenger(request.getPassengerId());

	        Ride ride = new Ride();

	        ride.setPassengerId(request.getPassengerId());
	        ride.setPickupLatitude(request.getPickupLatitude());
	        ride.setPickupLongitude(request.getPickupLongitude());
	        ride.setDestinationLatitude(
	            request.getDestinationLatitude()
	        );
	        ride.setDestinationLongitude(
	            request.getDestinationLongitude()
	        );

	        ride.setRideStatus(RideStatus.REQUESTED);

	        return convertToDTO(rideRepository.save(ride));
		 
	}

	// 2. Get a ride by ID
    @Transactional(readOnly = true)
    public RideResponseDTO getRideById(Long rideId) {

        return convertToDTO(findRide(rideId));
    }
    
    // 3. Get all rides with pagination
    @Transactional(readOnly = true)
    public Page<RideResponseDTO> getAllRides(
            int page, int size) {

        return rideRepository
            .findAll(createPageRequest(page, size))
            .map(this::convertToDTO);
    }
    
 // 4. Get passenger's ride history
    @Transactional(readOnly = true)
    public Page<RideResponseDTO> getRidesByPassenger(
            Long passengerId, int page, int size) {

        validatePositiveId(passengerId, "Passenger");

        return rideRepository
            .findByPassengerId(
                passengerId,
                createPageRequest(page, size)
            )
            .map(this::convertToDTO);
    }
    
    // 5. Get driver's ride history
    @Transactional(readOnly = true)
    public Page<RideResponseDTO> getRidesByDriver(
            Long driverId, int page, int size) {

        validatePositiveId(driverId, "Driver");

        return rideRepository
            .findByDriverId(
                driverId,
                createPageRequest(page, size)
            )
            .map(this::convertToDTO);
    }

    // 6. Search and assign a nearby driver
    @Transactional
    public RideResponseDTO assignDriver(Long rideId) {

        Ride ride = findRideForUpdate(rideId);

        if (ride.getRideStatus() != RideStatus.REQUESTED &&
            ride.getRideStatus() != RideStatus.SEARCHING) {

            throw new RideConflictException(
                "Cannot assign driver in current ride status"
            );
        }

        List<Map<String, Object>> drivers =
            driverClient.findNearbyDrivers(
                ride.getPickupLatitude(),
                ride.getPickupLongitude(),
                5.0
            );

        for (Map<String, Object> driver : drivers) {

            if (driver == null) {
                continue;
            }

            Object value = driver.get("id");

            if (!(value instanceof Number)) {
                continue;
            }

            Long driverId = ((Number) value).longValue();

            // Driver Service performs conditional reservation
            boolean reserved = driverClient.reserveDriver(
                    driverId, rideId
            );

            if (reserved) {

                ride.setDriverId(driverId);
                ride.setRideStatus(RideStatus.ASSIGNED);

                return convertToDTO(
                    rideRepository.save(ride)
                );
            }
        }

        // No available drivers
        ride.setRideStatus(RideStatus.SEARCHING);

        return convertToDTO(
            rideRepository.save(ride)
        );
    }

 // 7. Driver Accepts Ride
    @Transactional
    public RideResponseDTO acceptRide(Long rideId) {

        Ride ride = findRideForUpdate(rideId);

        requireStatus(ride, RideStatus.ASSIGNED);

        if (ride.getDriverId() == null) {
            throw new RideConflictException(
                "No driver assigned to this ride"
            );
        }

        ride.setRideStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(Instant.now());

        return convertToDTO(
            rideRepository.save(ride)
        );
    }

 // 8. Start Ride
    @Transactional
    public RideResponseDTO startRide(Long rideId) {

        Ride ride = findRideForUpdate(rideId);

        requireStatus(ride, RideStatus.ACCEPTED);

        ride.setRideStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(Instant.now());

        return convertToDTO(
            rideRepository.save(ride)
        );
    }

    // 9. Complete Ride
    @Transactional
    public RideResponseDTO completeRide(Long rideId) {

        Ride ride = findRideForUpdate(rideId);

        requireStatus(ride, RideStatus.IN_PROGRESS);

        if (ride.getDriverId() == null) {
            throw new RideConflictException(
                "No driver assigned to this ride"
            );
        }

        // Release reservation in Driver Service
        driverClient.releaseDriver(
            ride.getDriverId(), rideId
        );

        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(Instant.now());

        return convertToDTO(
            rideRepository.save(ride)
        );
    }

    // 10. Cancel Ride
    @Transactional
    public RideResponseDTO cancelRide(Long rideId) {

        Ride ride = findRideForUpdate(rideId);

        if (ride.getRideStatus() == RideStatus.COMPLETED ||
            ride.getRideStatus() == RideStatus.CANCELLED ||
            ride.getRideStatus() == RideStatus.IN_PROGRESS) {

            throw new RideConflictException(
                "Ride cannot be cancelled in current status"
            );
        }

        // Release driver when one is assigned
        if (ride.getDriverId() != null) {
            driverClient.releaseDriver(
                ride.getDriverId(), rideId
            );
        }

        ride.setRideStatus(RideStatus.CANCELLED);
        ride.setCancelledAt(Instant.now());

        return convertToDTO(
            rideRepository.save(ride)
        );
    }

 // Helper: Find Ride With Database Lock
    private Ride findRideForUpdate(Long id) {

    	validatePositiveId(id, "Ride");

        return rideRepository.findByIdForUpdate(id)
            .orElseThrow(() ->
                new RideNotFoundException(id)
            );
    }
    
    // Helper: Validate Ride Status
    private void requireStatus(
            Ride ride, RideStatus expectedStatus) {

        if (ride.getRideStatus() != expectedStatus) {

            throw new RideConflictException(
                "Expected ride status " + expectedStatus +
                " but found " + ride.getRideStatus()
            );
        }
    }
    
 // Helper: Pagination
    private PageRequest createPageRequest(int page, int size) {

        if (page < 0 || size < 1 || size > 100) {

            throw new InvalidRideRequestException(
                "Page must be >= 0 and size between 1 and 100"
            );
        }

        return PageRequest.of(
            page,
            size,
            Sort.by("id").descending()
        );
    }
    
 // Helper: Find ride
    private Ride findRide(Long rideId) {

        validatePositiveId(rideId, "Ride");

        return rideRepository.findById(rideId)
            .orElseThrow(() ->
                new RideNotFoundException(rideId)
            );
    }
    
 // Helper: Validate a positive ID
    private void validatePositiveId(Long id, String type) {

        if (id == null || id <= 0) {
            throw new InvalidRideRequestException(
                type + " ID must be positive"
            );
        }
    }
    
    
	 // Helper: Validate Passenger
    private void validatePassenger(Long passengerId) throws ServiceUnavailableException {

    	// Call PassengerClient to fetch passenger
        Map<?, ?> passenger =
                passengerClient.getPassengerById(passengerId);

        // Check for empty response
        if (passenger == null) {
            throw new ServiceUnavailableException(
                "Passenger Service returned an empty response"
            );
        }

        // Check account status
        if (!"ACTIVE".equals(passenger.get("accountStatus"))) {
            throw new InvalidRideRequestException(
                "Passenger must have an ACTIVE account"
            );
        }
    }

    // Helper: Validate Coordinates
    private void validateCoordinates(
            Double latitude, Double longitude) {

        if (latitude == null || longitude == null ||
            !Double.isFinite(latitude) ||
            !Double.isFinite(longitude) ||
            latitude < -90 || latitude > 90 ||
            longitude < -180 || longitude > 180) {

            throw new InvalidRideRequestException(
                "Invalid latitude or longitude"
            );
        }
    }

    // Helper: Entity To Response DTO
    private RideResponseDTO convertToDTO(Ride ride) {

        RideResponseDTO response = new RideResponseDTO();

        response.setId(ride.getId());
        response.setPassengerId(ride.getPassengerId());
        response.setDriverId(ride.getDriverId());

        response.setPickupLatitude(
            ride.getPickupLatitude()
        );
        response.setPickupLongitude(
            ride.getPickupLongitude()
        );

        response.setDestinationLatitude(
            ride.getDestinationLatitude()
        );
        response.setDestinationLongitude(
            ride.getDestinationLongitude()
        );

        response.setRideStatus(ride.getRideStatus());

        response.setRequestedAt(ride.getRequestedAt());
        response.setAcceptedAt(ride.getAcceptedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        response.setCancelledAt(ride.getCancelledAt());

        return response;
    }
    
    
    
    
    
    
}
