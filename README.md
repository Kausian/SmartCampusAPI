# Smart Campus Sensor & Room Management API

## Overview

This coursework project implements a RESTful **Smart Campus Sensor & Room Management API** using **JAX-RS** and **in-memory data structures** only. The system manages campus rooms, sensors assigned to rooms, and historical readings for each sensor.

The API supports:

* discovery metadata at the API root
* room creation, retrieval, listing, and safe deletion
* sensor creation with room validation
* sensor filtering by type
* nested sensor readings through a sub-resource
* custom exception mapping with clean JSON error responses

This implementation follows the coursework constraints by using:

* **JAX-RS only**
* **Tomcat 9** deployment
* **HashMap / ArrayList based in-memory storage**
* **no database**
* **no Spring Boot**

---

## Technology Stack

* Java
* Maven
* JAX-RS (`javax.ws.rs`)
* Tomcat 9
* In-memory data structures (`HashMap`, `ArrayList`)

---

## API Base URL

In this project, the deployed base URL is:

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

If your Tomcat context path is different, replace `SmartCampusAPI` with your deployed application name.

---

## Project Structure

```text
src/main/java/com/westminster/smartcampus/
├── config/
│   └── RestApplication.java
├── exception/
│   ├── LinkedResourceNotFoundException.java
│   ├── RoomNotEmptyException.java
│   └── SensorUnavailableException.java
├── filter/
│   └── ApiLoggingFilter.java
├── mapper/
│   ├── GlobalExceptionMapper.java
│   ├── LinkedResourceNotFoundMapper.java
│   ├── RoomNotEmptyMapper.java
│   └── SensorUnavailableMapper.java
├── model/
│   ├── ApiError.java
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── resource/
│   ├── DebugResource.java
│   ├── DiscoveryResource.java
│   ├── RoomResource.java
│   ├── SensorReadingResource.java
│   └── SensorResource.java
└── store/
    └── DataStore.java
```

---

## Data Models

### Room

```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 80,
  "sensorIds": ["CO2-001", "TEMP-001"]
}
```

### Sensor

```json
{
  "id": "CO2-001",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 420.0,
  "roomId": "LIB-301"
}
```

### SensorReading

```json
{
  "id": "generated-uuid",
  "timestamp": 1713870000000,
  "value": 455.7
}
```

---

## How to Build and Run

### Option 1 - Run in NetBeans with Tomcat 9

1. Open the project in NetBeans.
2. Make sure **Tomcat 9** is configured as the server.
3. Clean and build the project.
4. Run the project.
5. Open the base URL:

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

### Option 2 - Build with Maven

Run the following in the project directory:

```bash
mvn clean package
```

Then deploy the generated WAR file to Tomcat 9.

---

## Endpoint Summary

### Discovery

* `GET /api/v1`

### Rooms

* `GET /api/v1/rooms`
* `POST /api/v1/rooms`
* `GET /api/v1/rooms/{roomId}`
* `DELETE /api/v1/rooms/{roomId}`

### Sensors

* `GET /api/v1/sensors`
* `GET /api/v1/sensors?type=CO2`
* `POST /api/v1/sensors`
* `GET /api/v1/sensors/{sensorId}`

### Sensor Readings

* `GET /api/v1/sensors/{sensorId}/readings`
* `POST /api/v1/sensors/{sensorId}/readings`

### Debug

* `GET /api/v1/debug/error`

---

## Sample curl Commands

### 1. Discovery endpoint

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Get all rooms

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 3. Create a room

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 80
  }'
```

### 4. Create a valid sensor

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "CO2-001",
    "type": "CO2",
    "status": "ACTIVE",
    "currentValue": 420.0,
    "roomId": "LIB-301"
  }'
```

### 5. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 6. Add a reading to a sensor

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 455.7
  }'
```

### 7. Get sensor readings

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings
```

### 8. Trigger the global error mapper

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/debug/error
```

---

## Example Error Responses

### 409 Conflict - Room not empty

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Room LIB-301 still contains sensors",
  "path": "/api/v1/rooms",
  "timestamp": 1713870000000
}
```

### 422 Unprocessable Entity - Invalid room reference

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Cannot create sensor because room NO-ROOM does not exist",
  "path": "/api/v1/sensors",
  "timestamp": 1713870000000
}
```

### 403 Forbidden - Sensor under maintenance

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Sensor TEMP-003 is under maintenance",
  "path": "/api/v1/sensors",
  "timestamp": 1713870000000
}
```

### 500 Internal Server Error - Global exception mapper

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact the administrator.",
  "path": "unknown",
  "timestamp": 1713870000000
}
```

---

## Coursework Report Answers

### Part 1 - Service Architecture and Setup

#### 1. Resource lifecycle and synchronization

By default, a JAX-RS resource class is typically request-scoped, which means the runtime creates a new resource instance for each incoming request. This behaviour helps avoid shared mutable state inside resource classes themselves. However, this project stores data in shared in-memory collections inside a singleton-style `DataStore`, so thread safety still matters. To prevent race conditions and inconsistent updates, synchronized blocks are used around critical operations such as creating rooms, creating sensors, deleting rooms, and adding readings. This ensures that concurrent requests do not corrupt the shared `HashMap` and `ArrayList` structures.

#### 2. Why hypermedia is valuable in RESTful APIs

Hypermedia improves RESTful design because the API response itself tells clients where they can go next instead of forcing them to rely entirely on static external documentation. In this project, the discovery endpoint returns links to the main collections such as rooms and sensors. This approach helps client developers by making the API more self-descriptive, easier to explore, and easier to evolve over time if endpoints change or expand.

---

### Part 2 - Room Management

#### 1. Returning only IDs vs full room objects

Returning only room IDs reduces response size and saves network bandwidth, especially if there are many rooms. It is efficient when the client only needs a list of identifiers and will later request details for selected rooms. Returning full room objects is more convenient for clients because all the useful room metadata is available immediately, which reduces the need for extra requests. In this project, full room objects are returned because they improve usability and make the API easier to test and consume, while the payload remains small enough for the coursework scale.

#### 2. Is DELETE idempotent?

Yes, DELETE is treated as idempotent in the sense that deleting the same target repeatedly does not create additional side effects after the first successful deletion. In this implementation, the first successful DELETE removes the room and returns a success response. If the client sends the same DELETE again, the room no longer exists, so the API returns `404 Not Found`. The resource state does not change after the first deletion, which means the operation remains idempotent from a REST perspective.

---

### Part 3 - Sensor Operations and Linking

#### 1. What happens if the client sends the wrong content type?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the POST endpoint only accepts JSON request bodies. If a client sends data using a different media type such as `text/plain` or `application/xml`, JAX-RS will reject the request because the request content does not match the declared consumption type. In practice, this protects the API from unsupported input formats and ensures the method only processes data it can correctly deserialize into Java objects.

#### 2. Why query parameters are better for filtering

Using `@QueryParam` for filtering is generally better because filtering does not identify a different resource; it only narrows down a collection. The endpoint `/sensors?type=CO2` clearly means “give me the sensors collection, filtered by type.” In contrast, a path like `/sensors/type/CO2` makes the filter look like a completely different hierarchical resource. Query parameters are more flexible, more expressive for optional search criteria, and align better with common REST design practices.

---

### Part 4 - Deep Nesting with Sub-Resources

#### 1. Benefits of the sub-resource locator pattern

The sub-resource locator pattern keeps the API modular by delegating nested resource logic to a dedicated class. In this project, `SensorResource` handles sensor-level operations, while `SensorReadingResource` handles the readings that belong to a specific sensor. This separation makes the code easier to maintain, easier to test, and easier to extend. Without sub-resources, one large controller class would become crowded with unrelated methods and nested path handling logic, which would reduce clarity and increase complexity.

---

### Part 5 - Advanced Error Handling and Exception Mapping

#### 1. Why 422 is more accurate than 404 in this case

HTTP 422 is more semantically accurate because the overall request is syntactically valid and the target endpoint exists, but one value inside the JSON body is invalid. In this coursework, the sensor creation request is sent to a valid endpoint and the JSON format is correct, but the referenced `roomId` does not exist. A `404 Not Found` usually refers to the requested URI itself not existing, while `422 Unprocessable Entity` better expresses that the server understood the request but could not process the contained data.

#### 2. Risks of exposing Java stack traces

Exposing raw Java stack traces is a security risk because they reveal internal technical details about the application. An attacker could learn package names, class names, file names, framework behaviour, server structure, and the exact location of failures. This information can be used to map the internal design of the system and identify weak points for targeted attacks. By returning a generic JSON error through a global exception mapper, this project avoids leaking sensitive implementation details to external clients.

---

## Testing Summary

The API was tested using Postman against the coursework test flow, including:

* discovery endpoint
* room creation and deletion
* duplicate room conflict
* invalid sensor room validation
* sensor creation and filtering
* sub-resource readings retrieval and creation
* parent sensor `currentValue` synchronization
* maintenance sensor rejection
* global 500 error handling

All required test cases passed successfully.

---

## Video Demonstration

**Blackboard Submission:** Video uploaded separately as required by the coursework instructions.

---

## Author

**Senthan Kausian**
University of Westminster
Student ID: **W2120632**
