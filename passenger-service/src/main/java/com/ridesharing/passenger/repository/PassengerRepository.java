package com.ridesharing.passenger.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridesharing.passenger.Entity.Passenger;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

	// Find passenger by email
    Optional<Passenger> findByEmail(String email);

    // Find passengers by phone number
    List<Passenger> findByPhone(String phone);

    // Check if passenger email already exists
    boolean existsByEmail(String email);

    // Check duplicate email while updating
    boolean existsByEmailAndIdNot(String email, Long id);
}
