# College Placement Management System API

A RESTful backend service built with Java 21 and Spring Boot 4.1.1 for managing campus recruitment workflows. The system provides role-based access control for students and corporate recruiters, handling authentication, student academic profiles, company listings, job postings, and automated application eligibility checks.

## Live Deployment

* **Interactive API Documentation (Swagger UI):** [https://placement-backend-production-8827.up.railway.app/swagger-ui/index.html](https://placement-backend-production-8827.up.railway.app/swagger-ui/index.html)
* **Root Endpoint:** Navigating to the base URL (`/`) automatically redirects to the Swagger UI dashboard.
* **Runtime Note:** The production service runs on Railway with serverless app sleeping enabled. The initial request after a period of inactivity may take 15 to 30 seconds while the container wakes up.

---

## Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 4.1.1 (Spring WebMVC, Spring Data JPA, Spring Validation, Spring Security, Spring Cache)
* **Database:** MySQL (`mysql-connector-j`, Hibernate ORM)
* **Caching:** Redis (`spring-boot-starter-data-redis`)
* **Authentication:** JSON Web Tokens (`jjwt` 0.11.5) and BCrypt password hashing
* **API Documentation:** SpringDoc OpenAPI 3.1 (`springdoc-openapi-starter-webmvc-ui` 3.1.1)
* **Build Tool:** Maven

---

## Architecture & Implementation Details

* **Joined Table Inheritance (`InheritanceType.JOINED`):** The abstract `User` entity manages shared identity and credential attributes (`userId`, `name`, `email`, `password`, `role`), while `Student` (`cgpa`, `@ElementCollection skills`) and `Company` (`industry`, `location`) persist role-specific fields in normalized tables joined via primary-foreign key relationships. Password hashes are excluded from all serialized API responses using `@JsonIgnore`.
* **Stateless JWT Authentication & RBAC:** `SecurityConfig` disables CSRF, enforces stateless session management (`SessionCreationPolicy.STATELESS`), and registers a custom `OncePerRequestFilter` (`JwtFilter`) ahead of `UsernamePasswordAuthenticationFilter`. Tokens are signed with HMAC-SHA256 (`HS256`) and expire after 10 hours. Route and method-level authorization (`@EnableMethodSecurity` and `@PreAuthorize`) restrict operations to `STUDENT` or `COMPANY` authorities.
* **Application Eligibility & Vacancy Management:** When a student applies for a job (`PlacementService.applyForJob`), the service checks for duplicate applications via `ApplicationRepository.existsByStudentAndJob`, verifies that the student's CGPA meets the job's `requiredCgpa` (throwing `IneligibleCGPAException` if lower), confirms available vacancies (`vacancies > 0`), decrements the vacancy count, and records the application with status `APPLIED`.
* **Redis Read-Through Caching & Eviction:** `RedisConfig` overrides default binary serialization with JSON serialization (`RedisSerializer.json()`) and enforces a 10-minute Time-To-Live (TTL). `GET /api/jobs` caches job listings under the `jobs` key (`@Cacheable`), while creating a job (`postJob`) or submitting an application (`applyForJob`) triggers `@CacheEvict(value = "jobs", allEntries = true)` so clients never read stale vacancy counts.
* **Input Validation & Centralized Exception Handling:** Request DTOs enforce payload constraints via Jakarta Bean Validation (`@Email`, `@Size(min = 8)`, `@NotBlank`, `@NotNull`, `@DecimalMin("0.0")`, `@DecimalMax("10.0")`, `@Min(1)`). `GlobalExceptionHandler` (`@RestControllerAdvice`) intercepts domain exceptions and validation errors to return structured JSON error responses (`timestamp`, `status`, `error`, `message`).

---

## Project Structure

```text
placement-backend/
├── src/main/java/com/college/placement_backend/
│   ├── config/
│   │   ├── RedisConfig.java              # JSON serialization & 10-min TTL cache config
│   │   └── SwaggerConfig.java            # OpenAPI 3.1 metadata configuration
│   ├── controller/
│   │   ├── AuthController.java           # Registration & JWT login endpoints
│   │   ├── CompanyController.java        # Company listing, profile update & deletion
│   │   ├── HomeController.java           # Root ("/") redirect to Swagger UI
│   │   ├── JobController.java            # Job creation, cached retrieval & deletion
│   │   └── StudentController.java        # Student listing, profile update & job application
│   ├── dto/
│   │   ├── CompanyRequestDTO.java        # Validation rules for company profile updates
│   │   ├── JobRequestDTO.java            # Validation rules for posting jobs
│   │   ├── LoginRequest.java             # Login payload DTO
│   │   ├── SignupRequest.java            # Registration payload DTO (@Email, @Size)
│   │   └── StudentRequestDTO.java        # Validation rules for student profile updates
│   ├── exception/
│   │   └── IneligibleCGPAException.java  # Custom domain exception for CGPA cutoffs
│   ├── model/
│   │   ├── User.java                     # Abstract base entity (JOINED inheritance)
│   │   ├── Student.java                  # Student entity (CGPA & skills collection)
│   │   ├── Company.java                  # Company entity (industry & location)
│   │   ├── Job.java                      # Job entity linked to Company (@ManyToOne)
│   │   └── Application.java              # Application join entity (Student + Job + status)
│   ├── repository/
│   │   ├── UserRepository.java           # User lookup by email
│   │   ├── StudentRepository.java        # Student CRUD operations
│   │   ├── CompanyRepository.java        # Company CRUD operations
│   │   ├── JobRepository.java            # Job CRUD operations
│   │   └── ApplicationRepository.java    # Duplicate application lookup
│   ├── security/
│   │   ├── CustomUserDetailsService.java # Bridges UserRepository with Spring Security
│   │   ├── JwtFilter.java                # Per-request Bearer token extraction & validation
│   │   ├── JwtUtil.java                  # HS256 token generation & claim parsing
│   │   └── SecurityConfig.java           # Stateless filter chain & BCrypt encoder
│   ├── service/
│   │   └── PlacementService.java         # Core job application & eligibility logic
│   ├── GlobalExceptionHandler.java       # @RestControllerAdvice error formatter
│   └── PlacementBackendApplication.java  # Application entry point
├── src/main/resources/
│   └── application.properties            # Database, Redis, JWT & proxy configuration
└── pom.xml                               # Maven dependencies & build configuration
```

---

## API Reference

### Authentication (`/api/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register a new `STUDENT` or `COMPANY` account |
| `POST` | `/api/auth/login` | Public | Authenticate user credentials and return a JWT |

### Students (`/api/students`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/students` | `COMPANY` | Retrieve all student profiles |
| `PUT` | `/api/students/{id}/profile` | `STUDENT` | Update student CGPA (`0.0` to `10.0`) and skills list |
| `POST` | `/api/students/{studentId}/apply/{jobId}` | `STUDENT` | Apply for a job posting (evicts `jobs` cache) |

### Companies (`/api/companies`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/companies` | `STUDENT` | Retrieve all registered companies |
| `PUT` | `/api/companies/{id}/profile` | `COMPANY` | Update company industry and location details |
| `DELETE` | `/api/companies/{id}` | `COMPANY` | Delete a company record by ID |

### Jobs (`/api/jobs`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/jobs` | Authenticated | List all job postings (cached in Redis under `jobs`) |
| `POST` | `/api/jobs` | `COMPANY` | Create a new job posting (evicts `jobs` cache) |
| `DELETE` | `/api/jobs/{id}` | `COMPANY` | Delete a job posting by ID |

---

## Sample Request Payloads

**1. Register Account (`POST /api/auth/register`)**
```json
{
  "name": "Aarav Sharma",
  "email": "aarav@college.edu",
  "password": "securePassword123",
  "role": "STUDENT"
}
```

**2. Authenticate (`POST /api/auth/login`)**
```json
{
  "email": "aarav@college.edu",
  "password": "securePassword123"
}
```
*Include the returned token in the `Authorization` header of subsequent requests as `Bearer <token>`.*

**3. Update Student Profile (`PUT /api/students/{id}/profile`)**
```json
{
  "name": "Aarav Sharma",
  "email": "aarav@college.edu",
  "cgpa": 8.6,
  "skills": ["Java", "Spring Boot", "MySQL", "Redis"]
}
```

**4. Create Job Posting (`POST /api/jobs`)**
```json
{
  "title": "Backend Software Engineer",
  "requiredCgpa": 7.5,
  "vacancies": 5,
  "companyId": 2
}
```

---

## Environment Variables

Configuration is managed in `src/main/resources/application.properties` via environment variable placeholders with local development defaults:

| Variable | Description | Local Fallback |
| :--- | :--- | :--- |
| `DB_URL` | MySQL JDBC connection URL | `jdbc:mysql://localhost:3306/placement_db?createDatabaseIfNotExist=true` |
| `DB_USER` | MySQL database username | `root` |
| `DB_PASS` | MySQL database password | `your_local_password` |
| `REDIS_HOST` | Redis server hostname | `localhost` |
| `REDIS_PORT` | Redis server port | `6379` |
| `JWT_SECRET` | Secret key (>= 32 chars / 256-bit) for signing JWTs | `some-long-local-dev-secret-at-least-32-characters` |

---

## Local Setup & Execution

### Prerequisites
* **JDK 21**
* **MySQL Server** listening on port `3306` (the database `placement_db` is created automatically if it does not exist)
* **Redis Server** listening on port `6379`

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/vivekwaldia/placement-backend.git](https://github.com/vivekwaldia/placement-backend.git)
   cd placement-backend
   ```

2. **Pass your environment variables and start the server using the Maven wrapper:**

   **Windows (PowerShell):**
   ```powershell
   $env:DB_PASS="your_mysql_password"; $env:JWT_SECRET="your_256_bit_secret_key_at_least_32_chars"; .\mvnw spring-boot:run
   ```

   **Linux / macOS (Bash):**
   ```bash
   DB_PASS="your_mysql_password" JWT_SECRET="your_256_bit_secret_key_at_least_32_chars" ./mvnw spring-boot:run
   ```

3. **Access the API:**
   * **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
   * **OpenAPI JSON Spec:** `http://localhost:8080/v3/api-docs`

---

## Contact

**Author:** Vivek  
**Email:** waldia.vivek@gmail.com
