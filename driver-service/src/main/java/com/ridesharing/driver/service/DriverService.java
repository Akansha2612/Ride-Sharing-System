package com.ridesharing.driver.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.ridesharing.driver.dto.DriverRequestDTO;
import com.ridesharing.driver.dto.DriverResponseDTO;
import com.ridesharing.driver.entity.Driver;
import com.ridesharing.driver.entity.Driver.AccountStatus;
import com.ridesharing.driver.entity.Driver.AvailabilityStatus;
import com.ridesharing.driver.entity.Driver.VerificationStatus;
import com.ridesharing.driver.repository.DriverRepository;
import com.ridesharing.driver.repository.VehicleRepository;
import com.ridesharing.driver.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DriverService {

	private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public DriverService(
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository) {

        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // 1. Register Driver
    public DriverResponseDTO registerDriver(
            DriverRequestDTO request) {

        String email = normalizeEmail(request.getEmail());
        String license = normalizeLicense(
            request.getLicenseNumber()
        );

        if (driverRepository.existsByEmail(email)) {
            throw new DuplicateResourceException(
                "Email already registered"
            );
        }

        if (driverRepository.existsByLicenseNumber(license)) {
            throw new DuplicateResourceException(
                "License already registered"
            );
        }

        Driver driver = new Driver();
        copyToEntity(request, driver);

        driver.setAccountStatus(AccountStatus.ACTIVE);
        driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        driver.setVerificationStatus(VerificationStatus.PENDING);

        return convertToDTO(driverRepository.save(driver));
    }
    
    // 2. Get Driver By ID
    @Transactional(readOnly = true)
    public DriverResponseDTO getDriverById(Long id) {
        return convertToDTO(findDriver(id));
    }
    
    @Transactional(readOnly = true)
    public Page<DriverResponseDTO> getAllDrivers(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDriverRequestException("Page must be >= 0, size between 1 and 100");
        }
        return driverRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()))
                .map(this::convertToDTO);
    }
    
    public DriverResponseDTO updateDriver(Long id, DriverRequestDTO request) {
        Driver driver = findDriver(id);
        String email = normalizeEmail(request.getEmail());
        String license = normalizeLicense(request.getLicenseNumber());
        if (driverRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (driverRepository.existsByLicenseNumberAndIdNot(license, id)) {
            throw new DuplicateResourceException("License number is already registered");
        }
        // Compare BEFORE copyToEntity changes the license number.
        boolean licenseChanged = !license.equals(driver.getLicenseNumber());
        if (licenseChanged && driver.getAvailabilityStatus() == AvailabilityStatus.ON_RIDE) {
            throw new InvalidDriverRequestException("Cannot change license during a ride");
        }
        copyToEntity(request, driver);
        if (licenseChanged) {
            driver.setVerificationStatus(VerificationStatus.PENDING);
            driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        }
        return convertToDTO(driverRepository.save(driver));
    }

    // Helper: Find Driver Entity
    private Driver findDriver(Long id) {

        return driverRepository.findById(id)
            .orElseThrow(() ->
                new DriverNotFoundException(id)
            );
    }

    public void deleteDriver(Long id) {
        Driver driver = findDriver(id);
        if (driver.getAvailabilityStatus() != AvailabilityStatus.OFFLINE) {
            throw new InvalidDriverRequestException("Driver must be OFFLINE before deletion");
        }
        if (vehicleRepository.existsByDriver_Id(id)) {
            throw new InvalidDriverRequestException("Delete registered vehicles first");
        }
        driverRepository.delete(driver);
    }
    
    @Transactional(readOnly = true)
    public List<DriverResponseDTO> searchDrivers(String email, String phone) {
        if ((email == null) == (phone == null)) {
            throw new InvalidDriverRequestException("Provide either email or phone, not both");
        }
        if (email != null) {
            if (email.isBlank()) throw new InvalidDriverRequestException("Email cannot be blank");
            return driverRepository.findByEmail(normalizeEmail(email)).stream()
                    .map(this::convertToDTO).toList();
        }
        if (phone.isBlank()) throw new InvalidDriverRequestException("Phone cannot be blank");
        return driverRepository.findByPhone(phone.trim()).stream()
                .map(this::convertToDTO).toList();
    }
    
    public DriverResponseDTO updateLocation(Long id, Double latitude, Double longitude) {
        validateCoordinates(latitude, longitude);
        Driver driver = findDriver(id);
        driver.setLatitude(latitude);
        driver.setLongitude(longitude);
        driver.setLocationUpdatedAt(Instant.now());
        return convertToDTO(driverRepository.save(driver));
    }
    
    public DriverResponseDTO updateAvailability(Long id, AvailabilityStatus status) {
        Driver driver = findDriver(id);
        if (status == null || status == AvailabilityStatus.ON_RIDE) {
            throw new InvalidDriverRequestException("Only ONLINE or OFFLINE may be set manually");
        }
        if (driver.getAvailabilityStatus() == AvailabilityStatus.ON_RIDE) {
            throw new InvalidDriverRequestException("Cannot manually change status during a ride");
        }
        if (status == AvailabilityStatus.ONLINE) {
            if (driver.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new InvalidDriverRequestException("Driver account must be ACTIVE");
            }
            if (driver.getVerificationStatus() != VerificationStatus.VERIFIED) {
                throw new InvalidDriverRequestException("Driver must be VERIFIED");
            }
            if (!vehicleRepository.existsByDriver_Id(id)) {
                throw new InvalidDriverRequestException("Register a vehicle first");
            }
            if (!hasRecentLocation(driver)) {
                throw new InvalidDriverRequestException("Update GPS location before going ONLINE");
            }
        }
        driver.setAvailabilityStatus(status);
        return convertToDTO(driverRepository.save(driver));
    }
    
 // DEVELOPMENT ONLY: restrict this route to authorized admins before deployment.
    public DriverResponseDTO updateAccountStatus(Long id, AccountStatus status) {
        Driver driver = findDriver(id);
        if (status == null) throw new InvalidDriverRequestException("Account status is required");
        if (driver.getAvailabilityStatus() == AvailabilityStatus.ON_RIDE) {
            throw new InvalidDriverRequestException("Resolve active ride before changing account status");
        }
        driver.setAccountStatus(status);
        if (status != AccountStatus.ACTIVE) driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        return convertToDTO(driverRepository.save(driver));
    }
    
    // DEVELOPMENT ONLY: restrict this route to authorized admins before deployment.
    public DriverResponseDTO updateVerification(Long id, VerificationStatus status) {
        Driver driver = findDriver(id);
        if (status == null) throw new InvalidDriverRequestException("Verification status is required");
        if (driver.getAvailabilityStatus() == AvailabilityStatus.ON_RIDE) {
            throw new InvalidDriverRequestException("Resolve active ride before changing verification");
        }
        driver.setVerificationStatus(status);
        if (status != VerificationStatus.VERIFIED) driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        return convertToDTO(driverRepository.save(driver));
    }
    
    @Transactional(readOnly = true)
    public List<DriverResponseDTO> nearbyDrivers(Double latitude, Double longitude, double radiusKm) {
        validateCoordinates(latitude, longitude);
        if (!Double.isFinite(radiusKm) || radiusKm <= 0 || radiusKm > 50) {
            throw new InvalidDriverRequestException("radiusKm must be greater than 0 and at most 50");
        }
        // Small-project search; for large-scale use PostGIS & spatial indexes.
        return driverRepository.findByAccountStatusAndAvailabilityStatusAndVerificationStatus(
                    AccountStatus.ACTIVE, AvailabilityStatus.ONLINE, VerificationStatus.VERIFIED)
                .stream()
                .filter(this::hasRecentLocation)
                .map(driver -> {
                    double distance = calculateDistance(latitude, longitude,
                            driver.getLatitude(), driver.getLongitude());
                    if (distance > radiusKm) return null;
                    DriverResponseDTO dto = convertToDTO(driver);
                    dto.setDistanceKm(Math.round(distance * 100.0) / 100.0);
                    return dto;
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(DriverResponseDTO::getDistanceKm))
                .limit(20)
                .toList();
    }

    // Helper: Normalize Email
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    
    

    // Helper: Normalize License
    private String normalizeLicense(String license) {
        return license.trim().toUpperCase(Locale.ROOT);
    }

    // Helper: Validate Coordinates
    private void validateCoordinates(
            Double latitude, Double longitude) {

        if (latitude == null || longitude == null ||
                !Double.isFinite(latitude) ||
                !Double.isFinite(longitude) ||
                latitude < -90 || latitude > 90 ||
                longitude < -180 || longitude > 180) {

            throw new InvalidDriverRequestException(
                "Invalid latitude or longitude"
            );
        }
    }
    
    /*How availability works
    A driver is allowed to go ONLINE only when their account is ACTIVE, their verification status is VERIFIED, they have at least one registered vehicle, and their location was updated within the last 10 minutes.
    The ON_RIDE state is reserved for future Ride Microservice integration. A separate atomic driver-reservation operation will be necessary to prevent two rides from selecting the same driver simultaneously.
    */
    // Helper: Recent GPS Location
    private boolean hasRecentLocation(Driver driver) {

        return driver.getLatitude() != null &&
            driver.getLongitude() != null &&
            driver.getLocationUpdatedAt() != null &&
            driver.getLocationUpdatedAt().isAfter(
                Instant.now().minus(10, ChronoUnit.MINUTES)
            );
    }

    // Haversine Distance Calculation (km)
    private double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2) {

        double latDiff = Math.toRadians(lat2 - lat1);
        double lonDiff = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(latDiff / 2), 2)
            + Math.cos(Math.toRadians(lat1))
            * Math.cos(Math.toRadians(lat2))
            * Math.pow(Math.sin(lonDiff / 2), 2);

        a = Math.max(0, Math.min(1, a));

        double distance = 6371.0088 * 2 *
            Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(distance * 100.0) / 100.0;
    }

    // Helper: Request DTO To Entity
    private void copyToEntity(
            DriverRequestDTO request, Driver driver) {

        driver.setName(request.getName().trim());
        driver.setEmail(normalizeEmail(request.getEmail()));
        driver.setPhone(request.getPhone().trim());
        driver.setLicenseNumber(
            normalizeLicense(request.getLicenseNumber())
        );

        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();

        if (latitude != null || longitude != null) {
            validateCoordinates(latitude, longitude);

            driver.setLatitude(latitude);
            driver.setLongitude(longitude);
            driver.setLocationUpdatedAt(Instant.now());
        }
    }

    // Helper: Entity To Response DTO
    private DriverResponseDTO convertToDTO(Driver driver) {

        DriverResponseDTO response = new DriverResponseDTO();

        response.setId(driver.getId());
        response.setName(driver.getName());
        response.setEmail(driver.getEmail());
        response.setPhone(driver.getPhone());

        response.setLatitude(driver.getLatitude());
        response.setLongitude(driver.getLongitude());
        response.setLocationUpdatedAt(
            driver.getLocationUpdatedAt()
        );

        response.setAccountStatus(driver.getAccountStatus());
        response.setAvailabilityStatus(
            driver.getAvailabilityStatus()
        );
        response.setVerificationStatus(
            driver.getVerificationStatus()
        );

        return response;
    }
    
    @Transactional
    public void reserveForRide(Long driverId, Long rideId) {

        if (driverId == null || driverId <= 0 ||
            rideId == null || rideId <= 0) {

            throw new InvalidDriverRequestException(
                "Valid driver ID and ride ID are required"
            );
        }

        int updated = driverRepository.reserveForRideIfAvailable(
            driverId,
            rideId,
            Driver.AccountStatus.ACTIVE,
            Driver.AvailabilityStatus.ONLINE,
            Driver.VerificationStatus.VERIFIED,
            Driver.AvailabilityStatus.ON_RIDE,
            Instant.now().minus(10, ChronoUnit.MINUTES)
        );

        if (updated != 1) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Driver is not available"
            );
        }
    }

    @Transactional
    public void releaseAfterRide(Long driverId, Long rideId) {

        if (driverId == null || driverId <= 0 ||
            rideId == null || rideId <= 0) {

            throw new InvalidDriverRequestException(
                "Valid driver ID and ride ID are required"
            );
        }

        int updated = driverRepository.releaseDriverForRide(
            driverId,
            rideId,
            Driver.AvailabilityStatus.ON_RIDE,
            Driver.AvailabilityStatus.OFFLINE
        );

        if (updated != 1) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Driver reservation does not match this ride"
            );
        }
    }
    
    
}
