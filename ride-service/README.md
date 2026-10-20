# Ride Microservice — Ride Sharing System

A Spring Boot REST API for creating rides, assigning nearby drivers, tracking ride status, cancelling rides, and retrieving passenger/driver ride history.

This README documents the **local development version** of the service discussed in this project. It assumes `RideService` uses the existing `PassengerClient` and `DriverClient` classes and that Driver Service implements the ride-ID-based reservation endpoints described below.

## 1. Technology and service configuration

| Item | Value |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot, Spring Web/RestClient, Spring Data JPA, Validation |
| Build tool | Maven |
| Database | PostgreSQL (`ride_db`) |
| IDE | Eclipse |
| API testing | Postman |
| Ride Service port | `8083` |
| Passenger Service port | `8081` |
| Driver Service port | `8082` |
| Ride API base URL | `http://localhost:8083/api/v1/rides` |

Each microservice has its **own database**. The Ride entity stores `passengerId` and `driverId` as identifiers, not JPA foreign-key associations to other microservice databases.

## 2. Main features

- Create a ride for an existing `ACTIVE` passenger.
- Validate pickup and destination coordinates.
- Search for nearby eligible drivers and attempt a conditional reservation.
- Accept, start, complete, or cancel a ride using controlled status transitions.
- View an individual ride, paginated rides, passenger ride history, and driver ride history.
- Return meaningful HTTP errors for invalid requests, missing rides, conflicts, and unavailable dependent services.

### Ride status lifecycle

```text
REQUESTED ── assign driver ───────────> ASSIGNED
    │                                    │
    ├── no available driver ─> SEARCHING  └── accept ──> ACCEPTED
    │                           │                         │
    │                           └── retry assignment ─────┘ (via ASSIGNED)
    │                                                     │
    │                                                     └── start ──> IN_PROGRESS ── complete ──> COMPLETED
    │
    └── cancel ──> CANCELLED
```

`CANCELLED` is also allowed from `SEARCHING`, `ASSIGNED`, and `ACCEPTED`. The current sample service does **not** allow cancellation after a ride reaches `IN_PROGRESS` or `COMPLETED`.

## 3. Project structure

```text
ride-service/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ridesharing/ride/
    │   │   ├── RideServiceApplication.java
    │   │   ├── entity/Ride.java
    │   │   ├── dto/RideRequestDTO.java
    │   │   ├── dto/RideResponseDTO.java
    │   │   ├── repository/RideRepository.java
    │   │   ├── service/PassengerClient.java
    │   │   ├── service/DriverClient.java
    │   │   ├── service/RideService.java
    │   │   ├── controller/RideController.java
    │   │   └── exception/
    │   │       ├── RideNotFoundException.java
    │   │       ├── InvalidRideRequestException.java
    │   │       ├── RideConflictException.java
    │   │       ├── ServiceUnavailableException.java
    │   │       └── GlobalExceptionHandler.java
    │   └── resources/application.properties
    └── test/
```

Keep `PassengerClient` and `DriverClient` in the package you actually declared. The examples above assume `com.ridesharing.ride.service`.

## 4. PostgreSQL setup

In pgAdmin, connect to the default `postgres` database and run:

```sql
CREATE USER ride_user WITH PASSWORD 'replace_with_your_local_password';
```

Then create the database (run separately if your SQL environment disallows `CREATE DATABASE` within a transaction):

```sql
CREATE DATABASE ride_db OWNER ride_user;
```

In `src/main/resources/application.properties`:

```properties
spring.application.name=ride-service
server.port=8083

spring.datasource.url=jdbc:postgresql://localhost:5432/ride_db
spring.datasource.username=ride_user
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

services.passenger.url=http://localhost:8081
services.driver.url=http://localhost:8082
```

Set `DB_PASSWORD` in **Eclipse → Run Configurations → Environment**, using your actual PostgreSQL password. Do not hardcode or commit passwords. `ddl-auto=update` is for **local development only**.

## 5. Start services in Eclipse

1. Start PostgreSQL.
2. Confirm that `passenger_db`, `driver_db`, and `ride_db` exist.
3. In Eclipse, right-click each Maven project and choose **Maven → Update Project**.
4. Run `PassengerServiceApplication` on port `8081`.
5. Run `DriverServiceApplication` on port `8082`.
6. Run `RideServiceApplication` on port `8083`.
7. Check the console for successful startup. If a port is already in use, stop the duplicate application.

**Dependency note:** The Ride Service needs Spring Data JPA, Spring Validation, PostgreSQL driver, and Spring's HTTP `RestClient` (provided by the appropriate Spring Web/RestClient dependency for your Boot version).

## 6. Ride REST API reference

All URLs below start with `http://localhost:8083`.

| Method | Path | Description | Expected success |
| --- | --- | --- | --- |
| `POST` | `/api/v1/rides` | Create ride | `201 Created` |
| `GET` | `/api/v1/rides/{id}` | Get ride by ID | `200 OK` |
| `GET` | `/api/v1/rides?page=0&size=10` | List rides, paginated | `200 OK` |
| `GET` | `/api/v1/rides/passenger/{passengerId}?page=0&size=10` | Passenger history | `200 OK` |
| `GET` | `/api/v1/rides/driver/{driverId}?page=0&size=10` | Driver history | `200 OK` |
| `POST` | `/api/v1/rides/{id}/assign` | Find/reserve nearby driver | `200 OK` |
| `PATCH` | `/api/v1/rides/{id}/accept` | Accept assigned ride | `200 OK` |
| `PATCH` | `/api/v1/rides/{id}/start` | Start accepted ride | `200 OK` |
| `PATCH` | `/api/v1/rides/{id}/complete` | Complete ride and release driver | `200 OK` |
| `PATCH` | `/api/v1/rides/{id}/cancel` | Cancel ride before it starts | `200 OK` |

The `assign` endpoint can return `rideStatus: "SEARCHING"` if no driver can be reserved; this is a successful HTTP request, not necessarily a successful assignment. All lifecycle PATCH requests have no JSON body in the current implementation.

## 7. Configure Postman environment

Create a Postman environment named **Ride Sharing Local** and set:

| Variable | Value |
| --- | --- |
| `rideBaseUrl` | `http://localhost:8083/api/v1/rides` |
| `passengerBaseUrl` | `http://localhost:8081/api/v1/passengers` |
| `driverBaseUrl` | `http://localhost:8082/api/v1/drivers` |
| `passengerId` | Leave blank initially |
| `driverId` | Leave blank initially |
| `vehicleId` | Leave blank initially |
| `rideId` | Leave blank initially |
| `cancelRideId` | Leave blank initially |

Select this environment in Postman. For requests with a JSON body, use **Body → raw → JSON**, so `Content-Type: application/json` is set.

To save a response ID automatically, use **Scripts → Post-response** (called **Tests** in some Postman versions), e.g.:

```javascript
if (pm.response.code === 201) {
    pm.environment.set("rideId", pm.response.json().id);
}
```

## 8. End-to-end Postman testing — in order

### Test 0 — Prepare an ACTIVE passenger

Check an existing passenger:

```http
GET {{passengerBaseUrl}}/1
```

If none exists, register one:

```http
POST {{passengerBaseUrl}}
Content-Type: application/json
```

```json
{
  "name": "Rahul Sharma",
  "email": "rahul.ride.demo@example.com",
  "phone": "9876543210",
  "latitude": 28.7041,
  "longitude": 77.1025
}
```

**Expected:** `201 Created`, a generated `id`, and `accountStatus: "ACTIVE"`. Save the real `id` to `passengerId`. Use a unique email on repeated tests.

Post-response script for the registration request:

```javascript
pm.test("Passenger created", () => pm.response.to.have.status(201));
if (pm.response.code === 201) {
    pm.environment.set("passengerId", pm.response.json().id);
}
```

### Test 0A — Prepare an ONLINE, VERIFIED driver

The next steps use **Driver Microservice (8082)**:

**Register driver**:

```http
POST {{driverBaseUrl}}
Content-Type: application/json
```

```json
{
  "name": "Amit Kumar",
  "email": "amit.ride.demo@example.com",
  "phone": "9876501234",
  "licenseNumber": "DL0420260098765",
  "latitude": 28.7041,
  "longitude": 77.1025
}
```

Expected `201 Created`. Save the generated `id` as `driverId`.

**Register vehicle**:

```http
POST {{driverBaseUrl}}/{{driverId}}/vehicles
Content-Type: application/json
```

```json
{
  "vehicleNumber": "DL01AB9876",
  "vehicleType": "CAR",
  "model": "Maruti Suzuki Dzire",
  "color": "White"
}
```

Expected `201 Created`. Save the vehicle ID if you want to test vehicle operations.

**Verify driver** (development-only administrative API):

```http
PATCH {{driverBaseUrl}}/{{driverId}}/verification?status=VERIFIED
```

Expected `200 OK` and `verificationStatus: "VERIFIED"`.

**Refresh GPS location**:

```http
PATCH {{driverBaseUrl}}/{{driverId}}/location
Content-Type: application/json
```

```json
{
  "latitude": 28.7041,
  "longitude": 77.1025
}
```

Expected `200 OK`. **Location should be updated within the last 10 minutes** when assigning a driver.

**Set ONLINE**:

```http
PATCH {{driverBaseUrl}}/{{driverId}}/availability?status=ONLINE
```

Expected `200 OK` and `availabilityStatus: "ONLINE"`.

### Test 1 — Create a ride

```http
POST {{rideBaseUrl}}
Content-Type: application/json
```

```json
{
  "passengerId": 1,
  "pickupLatitude": 28.7041,
  "pickupLongitude": 77.1025,
  "destinationLatitude": 28.6139,
  "destinationLongitude": 77.2090
}
```

**Replace `passengerId: 1` with the actual passenger ID saved in your environment.** In Postman raw JSON, you may instead write `"passengerId": {{passengerId}}` without quotes around the variable.

Expected `201 Created`. The full response resembles:

```json
{
  "id": 1,
  "passengerId": 1,
  "driverId": null,
  "pickupLatitude": 28.7041,
  "pickupLongitude": 77.1025,
  "destinationLatitude": 28.6139,
  "destinationLongitude": 77.2090,
  "rideStatus": "REQUESTED",
  "requestedAt": "2026-10-16T10:00:00Z",
  "acceptedAt": null,
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

IDs and timestamps shown here are **examples**, not fixed values.

Post-response script:

```javascript
pm.test("Ride created", () => pm.response.to.have.status(201));
if (pm.response.code === 201) {
    const body = pm.response.json();
    pm.test("Initial status is REQUESTED", () => {
        pm.expect(body.rideStatus).to.eql("REQUESTED");
    });
    pm.environment.set("rideId", body.id);
}
```

### Test 2 — Get ride by ID

```http
GET {{rideBaseUrl}}/{{rideId}}
```

Expected `200 OK` and the same ride ID/status. A nonexistent ID should produce `404 Not Found`.

### Test 3 — Get all rides (pagination)

```http
GET {{rideBaseUrl}}?page=0&size=10
```

Expected `200 OK` and a Spring Data page response containing a `content` array. Page numbers start at `0`.

### Test 4 — Passenger ride history

```http
GET {{rideBaseUrl}}/passenger/{{passengerId}}?page=0&size=10
```

Expected `200 OK` and a page of that passenger's rides, including the ride you just created.

### Test 5 — Assign nearby driver

Make sure the driver is still ONLINE, VERIFIED, ACTIVE, has a registered vehicle, and their location is recent.

```http
POST {{rideBaseUrl}}/{{rideId}}/assign
```

Expected `200 OK` with:

- `rideStatus: "ASSIGNED"` and a non-null `driverId` if reservation succeeds.
- **OR** `rideStatus: "SEARCHING"` and a null `driverId` if no eligible driver was reserved. You may refresh driver GPS/availability and retry.

Driver search uses a **5 km radius** in the current RideService code. After successful assignment, the Driver Service should show `availabilityStatus: "ON_RIDE"`.

Optional test script:

```javascript
pm.test("Assignment request succeeded", () => pm.response.to.have.status(200));
if (pm.response.code === 200) {
    const body = pm.response.json();
    pm.expect(["ASSIGNED", "SEARCHING"]).to.include(body.rideStatus);
    if (body.rideStatus === "ASSIGNED") {
        pm.environment.set("driverId", body.driverId);
    }
}
```

### Test 6 — Accept ride

Only run after `ASSIGNED`:

```http
PATCH {{rideBaseUrl}}/{{rideId}}/accept
```

Expected `200 OK`, `rideStatus: "ACCEPTED"`, and non-null `acceptedAt`.

### Test 7 — Start ride

Only run after `ACCEPTED`:

```http
PATCH {{rideBaseUrl}}/{{rideId}}/start
```

Expected `200 OK`, `rideStatus: "IN_PROGRESS"`, and non-null `startedAt`.

### Test 8 — Complete ride

Only run after `IN_PROGRESS`:

```http
PATCH {{rideBaseUrl}}/{{rideId}}/complete
```

Expected `200 OK`, `rideStatus: "COMPLETED"`, and non-null `completedAt`.

The Driver Service release operation is expected to set the driver's availability to `OFFLINE` and clear the matching `currentRideId`. Verify with:

```http
GET {{driverBaseUrl}}/{{driverId}}
```

The `currentRideId` field may not appear in the public Driver response DTO. You can verify it in `driver_db` instead.

### Test 9 — Driver ride history

```http
GET {{rideBaseUrl}}/driver/{{driverId}}?page=0&size=10
```

Expected `200 OK` with the assigned/completed ride in its `content` array.

### Test 10 — Cancel a separate ride

Create another ride using `POST {{rideBaseUrl}}`, then save its new ID as `cancelRideId`.

```http
PATCH {{rideBaseUrl}}/{{cancelRideId}}/cancel
```

Expected `200 OK`, `rideStatus: "CANCELLED"`, and non-null `cancelledAt`.

For the simple cancellation test, cancel from `REQUESTED`. To test **driver release on cancellation**, first make a driver ONLINE again, assign the second ride, and then cancel it while `ASSIGNED` or `ACCEPTED`.

## 9. Negative / error testing

| Scenario | How to test | Expected HTTP status |
| --- | --- | --- |
| Ride ID does not exist | `GET {{rideBaseUrl}}/999999` | `404` |
| Passenger ID does not exist | Create ride with nonexistent `passengerId` | `400` with current `PassengerClient` |
| Passenger is `BLOCKED` or `INACTIVE` | Create ride for that account | `400` |
| Missing passenger ID | Omit `passengerId` in request JSON | `400` |
| Invalid pickup latitude | Set `pickupLatitude` to `95` | `400` |
| Invalid destination longitude | Set `destinationLongitude` to `190` | `400` |
| Same pickup and destination | Supply identical coordinates | `400` |
| Invalid page or size | `?page=-1&size=10` or `?size=101` | `400` |
| Accept before assignment | Call `/accept` while `REQUESTED` | `409` |
| Start before acceptance | Call `/start` while `ASSIGNED` | `409` |
| Complete before ride starts | Call `/complete` while `ACCEPTED` | `409` |
| Cancel completed ride | Call `/cancel` after `COMPLETED` | `409` |
| Passenger Service unavailable | Stop port 8081 then create a ride | `503` with custom handler |
| Driver Service unavailable | Stop port 8082 then assign a ride | `503` with custom handler |

Error responses depend on your `GlobalExceptionHandler`. With the handler discussed in this project, an error typically resembles:

```json
{
  "status": 409,
  "message": "Expected ride status ASSIGNED but found REQUESTED"
}
```

## 10. Driver Service integration — required for assignment

The original Driver Service supports nearby search, but **assignment, completion, and cancellation of assigned rides require additional internal endpoints**:

```http
POST  /api/v1/drivers/{driverId}/reserve?rideId={rideId}
PATCH /api/v1/drivers/{driverId}/release?rideId={rideId}
```

Expected reservation behavior:

- Reserve only an `ACTIVE`, `VERIFIED`, `ONLINE` driver with a registered vehicle and a recent GPS update.
- Reserve conditionally so only one competing ride succeeds.
- Set `availabilityStatus` to `ON_RIDE` and `currentRideId` to the requesting ride's ID.
- Release only if the current reservation belongs to the supplied `rideId`.
- On release, set availability to `OFFLINE` and clear `currentRideId`.

If your Driver Service does **not** implement these endpoints, the Ride Service's GET/history/create APIs can still work, but the complete driver-assignment workflow cannot.

> **Internal-only:** Protect reservation/release endpoints with service authentication and authorization before deploying beyond local development.

## 11. Verify the PostgreSQL data

Connect to `ride_db` in pgAdmin and run:

```sql
SELECT
    id,
    passenger_id,
    driver_id,
    pickup_latitude,
    pickup_longitude,
    destination_latitude,
    destination_longitude,
    ride_status,
    requested_at,
    accepted_at,
    started_at,
    completed_at,
    cancelled_at
FROM rides
ORDER BY id DESC;
```

To check the reservation in Driver Service, connect to `driver_db` and run (after applying the reservation patch):

```sql
SELECT
    id,
    availability_status,
    verification_status,
    account_status,
    current_ride_id,
    location_updated_at
FROM drivers
ORDER BY id;
```

Hibernate's column names should match these defaults if you're using the usual Spring Boot naming strategy; inspect your actual schema if you customized column mappings.

## 12. Troubleshooting

| Symptom | Possible cause and fix |
| --- | --- |
| `passengerClient.get().uri(...)` compile error | `passengerClient` is your **custom `PassengerClient` class**, not `RestClient`. Call `passengerClient.getPassengerById(passengerId)` or `validateActivePassenger(passengerId)` instead. |
| `ServiceUnavailableException` import/error | Do **not** use `javax.naming.ServiceUnavailableException`. Use `com.ridesharing.ride.exception.ServiceUnavailableException` extending `RuntimeException`. |
| `RestClient.Builder` bean not found | Verify the Spring Boot / Web-REST client dependency and Maven imports, then update the Maven project. |
| `404` on a Ride URL | Check controller `@RequestMapping("/api/v1/rides")`, port `8083`, and endpoint method/path. |
| `503` when creating a ride | Passenger Service (8081) may be stopped, configured URL may be incorrect, or the HTTP call failed. |
| Assignment stays `SEARCHING` | Driver may not be ACTIVE + VERIFIED + ONLINE, may lack a vehicle, may be more than 5 km away, or GPS is more than 10 minutes old. |
| `404` for Driver `/reserve` | The reservation endpoint has not yet been added to Driver Service. |
| `409` on accept/start/complete | The ride is in the wrong status. Fetch the ride with `GET /api/v1/rides/{id}` first. |
| Database connection fails | Check PostgreSQL, `ride_db`, `ride_user`, `DB_PASSWORD`, and `application.properties`. |
| POST returns `400` | Check `Content-Type: application/json`, required fields, active passenger, and valid coordinates. |

## 13. Current limitations and production work

This is a **learning/local-development implementation**. It is not yet a production-grade ride-dispatch platform.

- The Ride and Driver databases have **independent transactions**. `@Transactional` in Ride Service does not make external Driver Service calls atomic. A timeout, crash or failed database commit can leave ride and driver states inconsistent.
- Add **idempotency keys, reservation ownership, HTTP timeouts, retry policies and reconciliation** to recover partial failures.
- Add authentication and role-based authorization: only the correct driver should accept/start/complete, and only authorized passengers or administrators should cancel.
- Do not expose passenger/driver phone numbers and precise location data without authorization.
- Move schema evolution from `ddl-auto=update` to database migrations (for example Flyway).
- Add unit tests, integration tests, and concurrency tests for assignment and release.
- Keep the selected vehicle with each assignment, if your business rules allow multiple registered vehicles per driver.

## 14. Save and maintain this README

Put this file at the root of `ride-service`, beside `pom.xml`:

```text
ride-service/
├── pom.xml
├── README.md
└── src/
```

In Eclipse: **Right-click `ride-service` → New → File → `README.md`**, paste this content, and press **Ctrl + S**.

You can also export your Postman collection to a `postman/` folder and keep it with the README. Commit both to Git, excluding passwords and sensitive environment variables.
