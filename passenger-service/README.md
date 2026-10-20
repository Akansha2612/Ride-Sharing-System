
# Passenger Microservice — Ride Sharing System

## 1. Project Overview

The Passenger Microservice is responsible for managing passenger information in the Ride Sharing System.

### Features

- Register new passengers
- Get passenger details by ID
- Get all passengers with pagination
- Update passenger profiles
- Delete passengers
- Search passengers by email or phone
- Store passenger latitude and longitude
- Update passenger GPS location
- Manage passenger account status
- Validate input and handle API errors

## 2. Technology Stack

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Maven
- Eclipse IDE
- Postman

## 3. Application Configuration

Application Name: passenger-service

Server Port: 8081

Database Name: passenger_db

Database User: passenger_user

Base URL:

http://localhost:8081/api/v1/passengers

### application.properties

```properties
spring.application.name=passenger-service
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/passenger_db
spring.datasource.username=passenger_user
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
```

Note: ddl-auto=update is for local development.

## 4. How to Run the Project

1. Start PostgreSQL.
2. Confirm that passenger_db exists.
3. Open passenger-service in Eclipse.
4. Configure the DB_PASSWORD environment variable.
5. Run PassengerServiceApplication.java.
6. Confirm the application starts on port 8081.
7. Open Postman to test the APIs.

## 5. Passenger Entity Fields

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Passenger ID |
| name | String | Passenger name |
| email | String | Unique email address |
| phone | String | Phone number |
| latitude | Double | Last-known latitude |
| longitude | Double | Last-known longitude |
| locationUpdatedAt | Instant | Last GPS update |
| accountStatus | Enum | Passenger account status |

### Account Status Values

ACTIVE: Passenger account is enabled.

INACTIVE: Passenger account is deactivated.

BLOCKED: Passenger account is restricted.

New passengers are registered with ACTIVE status.

## 6. Passenger API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/passengers | Register passenger |
| GET | /api/v1/passengers | Get all passengers |
| GET | /api/v1/passengers/{id} | Get passenger by ID |
| PUT | /api/v1/passengers/{id} | Update passenger |
| DELETE | /api/v1/passengers/{id} | Delete passenger |
| GET | /api/v1/passengers/search | Search passenger |
| PATCH | /api/v1/passengers/{id}/location | Update location |
| PATCH | /api/v1/passengers/{id}/status | Update account status |

## 7. Postman Environment

Create an environment called Ride Sharing Local.

Add these variables:

baseUrl = http://localhost:8081/api/v1/passengers

passengerId = 1

Replace passengerId with the ID returned during registration.

For JSON requests, select:

Body -> raw -> JSON

## 8. Postman Testing

### Test 1: Register Passenger

Method: POST

URL:
{{baseUrl}}

Request Body:

```json
{
  "name": "Rahul Sharma",
  "email": "rahul.passenger@example.com",
  "phone": "9876543210",
  "latitude": 28.7041,
  "longitude": 77.1025
}
```

Expected Status: 201 Created

Expected Result:
- New passenger ID is generated.
- Passenger details are saved.
- accountStatus is ACTIVE.
- Latitude and longitude are saved.
- locationUpdatedAt is generated.

Sample Response (illustrative):

```json
{
  "id": 1,
  "name": "Rahul Sharma",
  "email": "rahul.passenger@example.com",
  "phone": "9876543210",
  "latitude": 28.7041,
  "longitude": 77.1025,
  "locationUpdatedAt": "2026-10-11T10:00:00Z",
  "accountStatus": "ACTIVE"
}
```

The actual ID and timestamp will be different.

### Test 2: Get Passenger By ID

Method: GET

URL:
{{baseUrl}}/{{passengerId}}

Expected Status: 200 OK

Expected Result:
Passenger details are returned.

Negative Test:

GET {{baseUrl}}/99999

Expected: 404 Not Found
(If that ID does not exist.)

### Test 3: Get All Passengers

Method: GET

URL:
{{baseUrl}}?page=0&size=10

Expected Status: 200 OK

Expected Result:
Paginated list of passengers.

Pagination Examples:

?page=0&size=5
?page=1&size=5
?page=0&size=10

Invalid Pagination:

?page=-1&size=10

Expected: 400 Bad Request

### Test 4: Search Passenger By Email

Method: GET

URL:
{{baseUrl}}/search?email=rahul.passenger@example.com

Expected Status: 200 OK

Expected Result:
Array containing the matching passenger.

If no passenger matches, the result should be [].

### Test 5: Search Passenger By Phone

Method: GET

URL:
{{baseUrl}}/search?phone=9876543210

Expected Status: 200 OK

Expected Result:
List of passengers with the specified phone number.

Note:
Provide either email or phone, not both.

### Test 6: Update Passenger Profile

Method: PUT

URL:
{{baseUrl}}/{{passengerId}}

Request Body:

```json
{
  "name": "Rahul Kumar Sharma",
  "email": "rahul.passenger@example.com",
  "phone": "9876543210",
  "latitude": 28.7041,
  "longitude": 77.1025
}
```

Expected Status: 200 OK

Expected Result:
Passenger name changes to Rahul Kumar Sharma.

Existing passenger ID and account status remain unchanged.

If both coordinates are omitted, the previous
location is preserved.

### Test 7: Update Passenger Location

Method: PATCH

URL:
{{baseUrl}}/{{passengerId}}/location

Request Body:

```json
{
  "latitude": 28.6139,
  "longitude": 77.2090
}
```

Expected Status: 200 OK

Expected Result:
- Latitude updated to 28.6139
- Longitude updated to 77.2090
- locationUpdatedAt refreshed

### Test 8: Invalid Passenger Location

Method: PATCH

URL:
{{baseUrl}}/{{passengerId}}/location

Request Body:

```json
{
  "latitude": 100.5,
  "longitude": 200.0
}
```

Expected Status: 400 Bad Request

Valid coordinate ranges:

Latitude: -90 to 90

Longitude: -180 to 180

### Test 9: Deactivate Passenger Account

Method: PATCH

URL:
{{baseUrl}}/{{passengerId}}/status?status=INACTIVE

Request Body: None

Expected Status: 200 OK

Expected:
accountStatus = INACTIVE

### Test 10: Block Passenger Account

Method: PATCH

URL:
{{baseUrl}}/{{passengerId}}/status?status=BLOCKED

Request Body: None

Expected Status: 200 OK

Expected:
accountStatus = BLOCKED

### Test 11: Reactivate Passenger Account

Method: PATCH

URL:
{{baseUrl}}/{{passengerId}}/status?status=ACTIVE

Request Body: None

Expected Status: 200 OK

Expected:
accountStatus = ACTIVE

Important:
Status changing is an administrative operation.
Restrict this endpoint before production deployment.

Also, the Ride Microservice must check passenger
eligibility before accepting a new ride request.

### Test 12: Delete Passenger

Method: DELETE

URL:
{{baseUrl}}/{{passengerId}}

Expected Status: 204 No Content

Verify deletion:

GET {{baseUrl}}/{{passengerId}}

Expected: 404 Not Found

## 9. Negative Test Cases

| Test Case | Expected HTTP Status |
|-----------|----------------------|
| Register duplicate email | 409 Conflict |
| Invalid email format | 400 Bad Request |
| Blank passenger name | 400 Bad Request |
| Invalid phone number | 400 Bad Request |
| Passenger ID not found | 404 Not Found |
| Invalid latitude | 400 Bad Request |
| Invalid longitude | 400 Bad Request |
| Only one coordinate provided | 400 Bad Request |
| Invalid pagination | 400 Bad Request |
| Search using email and phone together | 400 Bad Request |
| Search without email or phone | 400 Bad Request |

## 10. PostgreSQL Verification

Open pgAdmin and connect to passenger_db.

View all passengers:

```sql
SELECT * FROM passengers;
```

Check passenger location and account status:

```sql
SELECT
    id,
    name,
    email,
    phone,
    latitude,
    longitude,
    location_updated_at,
    account_status
FROM passengers;
```

Find active passengers:

```sql
SELECT *
FROM passengers
WHERE account_status = 'ACTIVE';
```

Find blocked passengers:

```sql
SELECT *
FROM passengers
WHERE account_status = 'BLOCKED';
```

## 11. Business Rules

- Passenger email must be unique.
- Passenger name cannot be blank.
- Email must have a valid format.
- Phone number must have a valid format.
- Latitude and longitude are optional during registration.
- When supplying coordinates, both must be valid.
- New passenger account status defaults to ACTIVE.
- Updating location refreshes the location timestamp.
- Updating a passenger profile does not change account status.
- Account status is managed through a separate endpoint.

## 12. How This Integrates With Ride Service

Passenger Microservice:
- Stores passenger profiles.
- Stores last-known passenger location.
- Stores passenger account status.

Ride Microservice:
- Verifies the passenger exists.
- Checks that the passenger is ACTIVE.
- Accepts pickup and destination coordinates.
- Creates and manages ride bookings.
- Finds available drivers through Driver Service.

Note:
The Ride Service should store its own pickup
location snapshot instead of relying only on
the passenger's latest GPS coordinates.

## 13. Security Notes

This implementation is for local development.

Before production:
- Add Spring Security and JWT authentication.
- Restrict profile and location access to authorized users.
- Restrict account-status changes to administrators.
- Prevent public access to passenger GPS coordinates.
- Add automated tests and database migrations.
- Do not commit database passwords to Git.

## 14. Future Improvements

- Saved Home and Work addresses
- Authentication and authorization
- Passenger preferences
- Automated unit and integration tests
- Swagger/OpenAPI documentation
- Flyway migrations
- Ride Microservice integration
