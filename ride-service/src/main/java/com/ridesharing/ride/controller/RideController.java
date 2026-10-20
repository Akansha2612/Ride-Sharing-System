package com.ridesharing.ride.controller;

import javax.naming.ServiceUnavailableException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ridesharing.ride.dto.RideRequestDTO;
import com.ridesharing.ride.dto.RideResponseDTO;
import com.ridesharing.ride.service.RideService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

	private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }
    
    // 1. Create Ride
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RideResponseDTO requestRide (@Valid @RequestBody RideRequestDTO request) throws ServiceUnavailableException
    {
    	return rideService.requestRide(request);
    }
    
    // 2. Get All Rides
    @GetMapping
    public Page<RideResponseDTO> getAllRides(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return rideService.getAllRides(page, size);
    }
    
 // 3. Get Ride By ID
    @GetMapping("/{id}")
    public RideResponseDTO getRideById(
            @PathVariable Long id) {

        return rideService.getRideById(id);
    }
    
    // 4. Passenger Ride History
    @GetMapping("/passenger/{passengerId}")
    public Page<RideResponseDTO> getPassengerRides(
            @PathVariable Long passengerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return rideService.getRidesByPassenger(
                passengerId, page, size);
    }
    
    // 5. Driver Ride History
    @GetMapping("/driver/{driverId}")
    public Page<RideResponseDTO> getDriverRides(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return rideService.getRidesByDriver(
                driverId, page, size);
    }
    
 // 6. Assign Driver
    @PostMapping("/{id}/assign")
    public RideResponseDTO assignDriver(
            @PathVariable Long id) {

        return rideService.assignDriver(id);
    }
    
    
    // 7. Accept Ride
    @PatchMapping("/{id}/accept")
    public RideResponseDTO acceptRide(
            @PathVariable Long id) {

        return rideService.acceptRide(id);
    }

    // 8. Start Ride
    @PatchMapping("/{id}/start")
    public RideResponseDTO startRide(
            @PathVariable Long id) {

        return rideService.startRide(id);
    }
    
 // 9. Complete Ride
    @PatchMapping("/{id}/complete")
    public RideResponseDTO completeRide(
            @PathVariable Long id) {

        return rideService.completeRide(id);
    }

    
 // 10. Cancel Ride
    @PatchMapping("/{id}/cancel")
    public RideResponseDTO cancelRide(
            @PathVariable Long id) {

        return rideService.cancelRide(id);
    }
    
    
    
    
    
    
}
