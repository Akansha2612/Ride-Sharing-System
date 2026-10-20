package com.ridesharing.driver.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import com.ridesharing.driver.dto.VehicleRequestDTO;
import com.ridesharing.driver.dto.VehicleResponseDTO;
import com.ridesharing.driver.service.VehicleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/drivers/{driverId}/vehicles")
public class VehicleController {
	 private final VehicleService vehicleService;

	    public VehicleController(VehicleService vehicleService) {
	        this.vehicleService = vehicleService;
	    }

	    @PostMapping
	    @ResponseStatus(HttpStatus.CREATED)
	    public VehicleResponseDTO add(@PathVariable Long driverId,
	            @Valid @RequestBody VehicleRequestDTO request) {
	        return vehicleService.addVehicle(driverId, request);
	    }
	    
	    @GetMapping
	    public List<VehicleResponseDTO> all(@PathVariable Long driverId) {
	        return vehicleService.getVehicles(driverId);
	    }

	    @GetMapping("/{vehicleId}")
	    public VehicleResponseDTO byId(@PathVariable Long driverId, @PathVariable Long vehicleId) {
	        return vehicleService.getVehicle(driverId, vehicleId);
	    }

	    @PutMapping("/{vehicleId}")
	    public VehicleResponseDTO update(@PathVariable Long driverId, @PathVariable Long vehicleId,
	            @Valid @RequestBody VehicleRequestDTO request) {
	        return vehicleService.updateVehicle(driverId, vehicleId, request);
	    }

	    @DeleteMapping("/{vehicleId}")
	    @ResponseStatus(HttpStatus.NO_CONTENT)
	    public void delete(@PathVariable Long driverId, @PathVariable Long vehicleId) {
	        vehicleService.deleteVehicle(driverId, vehicleId);
	    }
}
