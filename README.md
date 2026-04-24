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

#### 1. Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures $(maps/lists)$ to prevent data loss or race conditions.

A JAX-RS resource class is also by default request-scoped which implies that the runtime instantiates a new instance of a resource every time a request is received. This behaviour is used to prevent shared mutable state within resources classes themselves. Nonetheless, this project is storing the data in shared in-memory collections within a singleton-based `DataStore`, thus thread safety remains a concern. To avoid race conditions and inconsistent updates, synchronized blocks are implemented in and around critical operations like creating rooms, creating sensors, deleting rooms, and adding readings. This makes sure that the shared `HashMap` and `ArrayList` structures are not corrupted by concurrent requests.

#### 2. Question: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

Hypermedia improves RESTful design because the API response itself tells clients where they can go next instead of forcing them to rely entirely on static external documentation. In this project, the discovery endpoint returns links to the main collections such as rooms and sensors. This approach helps client developers by making the API more self-descriptive, easier to explore, and easier to evolve over time if endpoints change or expand.

---

### Part 2 - Room Management

#### 1.  Question: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

Sending room IDs only saves network bandwidth, particularly when the number of rooms is large. It is cost effective when the client just requires a list of identifiers and will seek information about chosen rooms in future. It is more convenient to clients to deliver full room objects since all the useful room metadata is immediately available to them, and no additional requests are necessary. In this project, we are returning full room objects since they enhance usability and simplify the API to test and use, whereas the payload size is small enough to fit the coursework scale.

#### 2. Question: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, DELETE is considered idempotent in the sense that repeatedly deleting a target does not produce any extra side effects when it has already been deleted successfully. In this implementation, the initial successful DELETE will delete the room and will respond with a success. In case of a repeat of the DELETE by the client, the room is no longer in existence and therefore, the API will respond with `404 Not Found`. The first deletion does not alter the state of the resource, and this implies that the operation is idempotent in terms of REST.

---

### Part 3 - Sensor Operations and Linking

#### 1. Question: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation indicates JAX-RS that POST endpoint will only accept the request body in form of a JSON. When a client provides data in another media type `text/plain` or `application/xml` JAX-RS will refuse the request since the request content is not in line with the announced consumption type. Practically, this safeguards the API against invalid input formats and furthermore only allows the method to work with data that it can deserialize to Java objects properly.

#### 2. You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Using `@QueryParam` for filtering is generally better because filtering does not identify a different resource; it only narrows down a collection. The endpoint `/sensors?type=CO2` clearly means “give me the sensors collection, filtered by type.” In contrast, a path like `/sensors/type/CO2` makes the filter look like a completely different hierarchical resource. Query parameters are more flexible, more expressive for optional search criteria, and align better with common REST design practices.

---

### Part 4 - Deep Nesting with Sub-Resources

#### 1. Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

The sub-resource locator pattern maintains the modularity of API by outsourcing nested resource logic to a special class. `SensorResource` in this project can be used to represent sensor-level operations and `SensorReadingResource` can be used to represent the readings that are associated with a particular sensor. This division facilitates easier maintenance, easy testing and easy extension of the code. Sub-resources would ensure that a single large controller class is not overwhelmed with irrelevant methods and nested path control code, complicating the code and making it less readable.

---

### Part 5 - Advanced Error Handling and Exception Mapping

#### 1. Question: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?


The HTTP 422 is more semantically precise since the request as a whole is syntactically valid and the endpoint being targeted is present, but one of the values within the JSON body is not. The sensor creation request, transmitted to a valid endpoint in this coursework, has the right format (JSON) and is sent to a valid endpoint, but the specified `roomId` is not found. A `404 Not Found` typically means that the URI requested doesn't exist, whereas `422 Unprocessable Entity` is a more accurate reason that the server knew the request but was unable to handle the data in it.

#### 2. Question: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Publication of raw Java stack traces is a security vulnerability in that it provides internal technical information on the application. An attacker might get to know names of packages, name of classes, name of files, behaviour of frameworks, structure of the server, and the precise point of failures. It is possible to map the inside design of the system using this information and find weak points to target attacks. This project will not disclose sensitive implementation details to external clients since it returns a generic JSON error when an exception occurs via a global exception mapper.

#### 3. Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

In this project, API observability is implemented using a custom JAX-RS filter that handles both incoming requests and outgoing responses. The request filter logs the HTTP method and request URI for every incoming API call, while the response filter logs the final HTTP status code returned to the client. Using JAX-RS filters for logging is advantageous because logging is a cross-cutting concern that applies across the whole API. Centralising it inside filters avoids duplicating Logger.info() statements in every resource method, keeps the resource classes cleaner, improves maintainability, and makes it easier to apply consistent logging behaviour throughout the application.

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

**Video Link:** https://drive.google.com/file/d/1Xao1krOMiivtjqa79noaD5gnc0_wOhPd/view?usp=sharing

---

## Author

Name: **Senthan Kausian**
Student ID: **w2120632**
