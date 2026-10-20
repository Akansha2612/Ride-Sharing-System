package com.ridesharing.driver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridesharing.driver.entity.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
	// Find all vehicles registered by a driver
    List<Vehicle> findByDriver_Id(Long driverId);

    // Find a specific vehicle belonging to a driver
    Optional<Vehicle> findByIdAndDriver_Id(
            Long vehicleId, Long driverId);

    // Check whether driver has any registered vehicles
    boolean existsByDriver_Id(Long driverId);

    // Check duplicate vehicle number during registration
    boolean existsByVehicleNumber(String vehicleNumber);

    // Check duplicate vehicle number during update
    boolean existsByVehicleNumberAndIdNot(
            String vehicleNumber, Long vehicleId);
}
