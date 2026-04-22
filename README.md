# Smart Campus API

**Student Name:** Gamage Senuja Ranmith  
**Student ID:** 20232614 / w2120691  
**Module:** 5COSC022W Client-Server Architectures

---

## 1. Overview

This project is a RESTful Smart Campus API built in **Java** using **JAX-RS (Jersey)** and deployed on **Apache Tomcat**.

The API manages three main resources:

- **Rooms**
- **Sensors**
- **Sensor Readings**

The system supports:

- a versioned discovery endpoint at `/api/v1`
- room creation, listing, retrieval, and deletion
- sensor registration with room validation
- sensor filtering by type using query parameters
- sensor reading history using a sub-resource locator
- custom exception mapping for `409`, `422`, `403`, and `500`
- request and response logging using a JAX-RS filter

This project uses **in-memory Java data structures only** and does **not** use a database.

**Base URL**

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

---

## 2. Technology Stack

- Java
- JAX-RS (Jersey)
- Apache Tomcat
- Maven
- Apache NetBeans
- Postman
- In-memory collections (`ConcurrentHashMap`, `ArrayList`)

---

## 3. How to Build and Run

### Option 1: Run in NetBeans

1. Open the project in Apache NetBeans.
2. Make sure Apache Tomcat is selected as the server.
3. Right-click the project and choose **Clean and Build**.
4. Right-click the project and choose **Run**.
5. Open the discovery endpoint in the browser or Postman:

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

### Option 2: Build with Maven

Open a terminal in the project folder and run:

```bash
mvn clean install
```

This builds the WAR file inside the `target` folder.

---

## 4. Main API Endpoints

### Discovery
- `GET /api/v1`

### Rooms
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`

### Sensors
- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=CO2`
- `GET /api/v1/sensors/{sensorId}`
- `POST /api/v1/sensors`

### Sensor Readings
- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

---

## 5. Sample curl Commands

### 1. Discovery endpoint

```bash
curl -i http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Create a room

```bash
curl -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":40,\"sensorIds\":[]}"
```

### 3. Get all rooms

```bash
curl -i http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 4. Get one room

```bash
curl -i http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 5. Create a valid sensor

```bash
curl -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"LIB-301\"}"
```

### 6. Get all sensors

```bash
curl -i http://localhost:8080/SmartCampusAPI/api/v1/sensors
```

### 7. Filter sensors by type

```bash
curl -i "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 8. Add a reading

```bash
curl -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"value\":650.5}"
```

### 9. Get reading history

```bash
curl -i http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings
```

### 10. Trigger 422 Unprocessable Entity

```bash
curl -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"CO2-999\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"NO-SUCH-ROOM\"}"
```

### 11. Create a maintenance sensor

```bash
curl -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"MAINTENANCE\",\"currentValue\":0.0,\"roomId\":\"LIB-301\"}"
```

### 12. Trigger 403 Forbidden

```bash
curl -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"value\":22.4}"
```

### 13. Trigger 409 Conflict

```bash
curl -i -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 14. Trigger 500 Internal Server Error

```bash
curl -i -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Broken Room\",\"capacity\":10,\"sensorIds\":[]}"
```

---

## 6. Conceptual Report Answers

### Part 1 - Service Architecture & Setup

#### 1. Default lifecycle of a JAX-RS resource class

By default, JAX-RS resource classes are usually **request-scoped**. This means the runtime creates a new resource object for each incoming request and destroys it after the response is sent. This is safer than sharing one mutable resource instance across many requests.

Because of this, shared application data should not be stored as normal instance fields inside resource classes. In this project, shared state is stored in a separate `DataStore` class using in-memory collections. Since multiple clients can access the API at the same time, thread safety is important. Using `ConcurrentHashMap` helps reduce race conditions when many requests read or write shared data.

#### 2. Why hypermedia / HATEOAS is useful

Hypermedia is useful because the API can provide navigation information inside responses instead of forcing the client to rely only on external documentation. This makes the API more self-descriptive.

In this project, the discovery endpoint returns links to main resource collections such as rooms, sensors, and readings. This helps client developers understand how to use the API and which endpoints are available.

---

### Part 2 - Room Management

#### 3. Returning only room IDs vs returning full room objects

Returning only room IDs reduces response size and saves bandwidth, but it forces the client to send extra requests to fetch room details. Returning the full room objects increases response size, but it gives the client more useful data in a single request.

In this coursework, returning full room objects is reasonable because it makes the API easier to use and the dataset is relatively small.

#### 4. Is DELETE idempotent?

Yes, DELETE is idempotent. If a room exists and can be deleted, the first request removes it. If the same DELETE request is sent again, the room is already gone, so the server may return `404 Not Found`, but the system state remains the same: the room is still deleted.

This means repeating the same DELETE request does not continue changing the final state.

---

### Part 3 - Sensor Operations & Linking

#### 5. What happens if a client sends the wrong content type?

The annotation `@Consumes(MediaType.APPLICATION_JSON)` tells JAX-RS that the method only accepts JSON. If a client sends data using another format such as `text/plain` or `application/xml`, the framework normally rejects the request before the resource method runs.

The typical response is **`415 Unsupported Media Type`** because the payload format does not match what the endpoint consumes.

#### 6. Why query parameters are better than path parameters for filtering

A query parameter is better for filtering because it expresses an optional condition on a collection resource rather than a completely different resource path. For example, `/api/v1/sensors?type=CO2` still means “the sensors collection, filtered by type”.

This is more flexible and easier to extend later with more filters, such as `?type=CO2&status=ACTIVE`. A path like `/api/v1/sensors/type/CO2` is more rigid and treats filtering like a separate path structure.

---

### Part 4 - Deep Nesting with Sub-Resources

#### 7. Benefits of the sub-resource locator pattern

The sub-resource locator pattern helps separate responsibilities between classes. Instead of putting all sensor logic and reading logic into one large resource class, the main `SensorResource` delegates reading-related work to `SensorReadingResource`.

This improves readability, maintainability, and scalability. In larger APIs, this design prevents one controller from becoming too large and difficult to manage.

#### 8. Historical data management and side effect

The reading history is stored per sensor, and each new reading is appended to the list for that sensor. A successful POST of a new reading also updates the parent sensor’s `currentValue`.

This side effect is important because it keeps the latest sensor state consistent with the historical readings.

---

### Part 5 - Error Handling, Exception Mapping & Logging

#### 9. Why 422 is more accurate than 404

`422 Unprocessable Entity` is more accurate when the request body is syntactically valid but contains a semantically invalid reference. In this project, posting a sensor with a `roomId` that does not exist is not the same as requesting a missing URL.

The endpoint exists, the JSON is valid, but the linked resource inside the JSON is invalid. That is why `422` is more suitable than `404`.

#### 10. Risks of exposing Java stack traces

Exposing stack traces can leak sensitive internal information such as package names, class names, method names, file paths, frameworks, and library details. An attacker can use that information to understand the system’s internal structure and plan more targeted attacks.

Returning a generic JSON error response instead of a raw stack trace helps protect the application.

#### 11. Why filters are better for logging

Logging is a cross-cutting concern because it applies to many endpoints. Using JAX-RS filters allows request and response logging to be implemented once and applied automatically to every endpoint.

This avoids repeating logging code in every resource method and keeps the resource classes focused on business logic.

---

## 7. Video Demonstration Checklist

The video demonstration should show:

- `GET /api/v1`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms`
- `GET /api/v1/rooms/{id}`
- valid `POST /api/v1/sensors`
- invalid `POST /api/v1/sensors`
- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=CO2`
- `POST /api/v1/sensors/{id}/readings`
- `GET /api/v1/sensors/{id}/readings`
- `403 Forbidden`
- `409 Conflict`
- `422 Unprocessable Entity`
- `500 Internal Server Error`

---

## 8. Notes

- This project uses **JAX-RS only**.
- No Spring Boot was used.
- No database was used.
- All data is stored in memory using Java collections.
- Restarting the server clears stored data.
- The API was tested using Postman.
