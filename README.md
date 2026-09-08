# URL Shortener API

A RESTful URL shortening service built with Java and Spring Boot.

The application allows users to create shortened URLs, redirect through generated short codes, manage existing URLs, configure expiration dates, enable or disable links, and track the number of accesses.

## Features

* URL shortening with randomly generated Base62 codes
* HTTP redirection using short codes
* Automatic URL expiration
* Enable/disable shortened URLs
* Click counter
* Pagination and sorting
* Custom URL validation
* Global exception handling
* PostgreSQL persistence
* Database migrations with Flyway
* Docker-based PostgreSQL environment
* Automated tests with JUnit, Mockito and MockMvc

## Tech Stack

* Java 21
* Spring Boot
* Spring Web MVC
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Docker
* Docker Compose
* Maven
* Lombok
* Apache Commons Validator
* JUnit 5
* Mockito
* MockMvc

## Architecture

The project follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Main packages:

```text
src/main/java/com/alanpmz/urlshortener
├── controller
├── dto
├── exception
├── model
├── repository
├── service
│   └── impl
└── validation
```

Responsibilities are separated between the HTTP layer, business rules, persistence, validation and error handling.

## How It Works

When a URL is created, the application generates a random 7-character Base62 short code.

Example:

```text
https://example.com/some/very/long/url
```

may generate:

```text
abc12XZ
```

Accessing:

```text
GET /abc12XZ
```

returns an HTTP `302 Found` response pointing to the original URL.

The application also increments the URL access counter whenever a successful redirect occurs.

Short-code uniqueness is enforced by PostgreSQL. If a collision occurs during creation, the service automatically generates another code and retries the operation.

## API Endpoints

### Create a shortened URL

```http
POST /urls
Content-Type: application/json
```

Request:

```json
{
  "url": "https://example.com"
}
```

Example response:

```json
{
  "originalUrl": "https://example.com",
  "shortCode": "abc12XZ",
  "createdAt": "2026-09-08T12:00:00",
  "expiresAt": "2026-09-15T12:00:00",
  "clickCount": 0,
  "active": true
}
```

Response:

```text
201 Created
Location: /abc12XZ
```

---

### Redirect to the original URL

```http
GET /{shortCode}
```

Example:

```http
GET /abc12XZ
```

Response:

```text
302 Found
Location: https://example.com
```

A successful redirect also increments the URL click counter.

---

### List URLs

```http
GET /urls
```

Available query parameters:

| Parameter   | Default | Description                 |
| ----------- | ------: | --------------------------- |
| `page`      |     `0` | Page number                 |
| `size`      |     `5` | Number of elements per page |
| `sortBy`    |    `id` | Property used for sorting   |
| `direction` |   `ASC` | `ASC` or `DESC`             |

Example:

```http
GET /urls?page=0&size=10&sortBy=createdAt&direction=DESC
```

---

### Find URL by ID

```http
GET /urls/{id}
```

Example:

```http
GET /urls/1
```

---

### Update a URL

```http
PATCH /urls/{id}
Content-Type: application/json
```

The endpoint supports partial updates.

Example:

```json
{
  "expiresAt": "2026-12-31T23:59:59",
  "active": false
}
```

Fields that are not provided remain unchanged.

---

### Delete a URL

```http
DELETE /urls/{id}
```

Successful deletion returns:

```text
204 No Content
```

## Error Handling

The API uses centralized exception handling to provide consistent HTTP responses.

Examples include:

| Situation                     |            Status |
| ----------------------------- | ----------------: |
| Invalid URL                   | `400 Bad Request` |
| Invalid request body          | `400 Bad Request` |
| URL not found                 |   `404 Not Found` |
| Short-code generation failure |    `409 Conflict` |
| Expired or inactive URL       |        `410 Gone` |

Example error response:

```json
{
  "timestamp": "2026-09-08T12:00:00",
  "status": 404,
  "error": "URL not found",
  "path": "/urls/999"
}
```

Validation errors include information about the invalid fields.

## Running the Project

### Requirements

Make sure you have installed:

* Java 21
* Docker
* Docker Compose

The project includes Maven Wrapper, so a separate Maven installation is not required.

### 1. Clone the repository

```bash
git clone https://github.com/alanpmz/urlshortener.git
cd urlshortener
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

The development database is configured with:

```text
Database: urlshortener
Port:     5432
Username: postgres
Password: postgres
```

These credentials are intended for local development only.

### 3. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

The application will be available at:

```text
http://localhost:8080
```

Flyway automatically applies the database migrations when the application starts.

## Running the Tests

Make sure the PostgreSQL container is running:

```bash
docker compose up -d
```

Then run the complete test suite.

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

The project contains tests for:

* Service business rules
* Controller behavior
* HTTP endpoints with MockMvc
* Request validation
* Global exception handling
* URL validation

## Database

Flyway manages the database schema.

The initial migration creates the `tb_urls` table with:

```text
id
original_url
short_code
created_at
expires_at
click_count
active
```

Indexes are created for `short_code` and `expires_at`.

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate therefore validates the schema while Flyway remains responsible for creating and evolving it.

## Testing Strategy

The project uses different testing levels to keep tests focused.

```text
Unit Tests
   ↓
Service Tests with Mockito
   ↓
Web MVC Tests with MockMvc
   ↓
Spring Application Context Test
```

JUnit 5 is used as the test framework, Mockito isolates dependencies in unit tests, and MockMvc validates HTTP behavior without requiring a running web server.

## Project Goals

This project was developed to practice backend engineering concepts beyond basic CRUD operations, including:

* layered architecture
* REST API design
* persistence with JPA
* database constraints
* collision handling
* pagination and sorting
* custom Bean Validation
* centralized exception handling
* database migrations
* containerized development environments
* automated testing

## Author

**Alan Pereira Miguez**

GitHub: `@alanpmz`
