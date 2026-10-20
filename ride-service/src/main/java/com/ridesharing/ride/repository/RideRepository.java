package com.ridesharing.ride.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import com.ridesharing.ride.entity.Ride;

import jakarta.persistence.LockModeType;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

	  // Find rides booked by a passenger
    Page<Ride> findByPassengerId(
            Long passengerId,
            Pageable pageable);

    // Find rides assigned to a driver
    Page<Ride> findByDriverId(
            Long driverId,
            Pageable pageable);

    // Lock a ride during status changes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Ride r WHERE r.id = :id")
    Optional<Ride> findByIdForUpdate(
            @Param("id") Long id);


}
