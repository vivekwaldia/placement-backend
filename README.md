# College Placement Management System API

A RESTful backend service built with Java 21 and Spring Boot 4.1.1 for managing campus recruitment workflows. The system provides role-based and ownership-verified access control for students and corporate recruiters, handling authentication, student academic profiles, company listings, job postings, automated CGPA eligibility checks, and end-to-end application tracking.

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
* **API Documentation:** SpringDoc OpenAPI 3.1 (`springdoc-openapi-starter-webmvc-ui` 3.1.1) with Bearer JWT authentication support
* **Build Tool:** Maven

---

## Architecture & Implementation Details

* **Joined Table Inheritance (`InheritanceType.JOINED`):** The abstract `User` entity manages shared identity and credential attributes (`userId`, `name`, `email` with `@Column(unique = true, nullable = false)`, `password`, `role`), while `Student` (`cgpa`, eager-fetched `@ElementCollection skills`) and `Company` (`industry`, `location`) persist role-specific fields in normalized tables joined via primary-foreign key relationships. Password hashes are excluded from all serialized API responses using `@JsonIgnore`.
* **Stateless JWT Authentication & Object-Level Ownership:** `SecurityConfig` disables CSRF, enforces stateless session management (`SessionCreationPolicy.STATELESS`), and registers a custom `OncePerRequestFilter` (`JwtFilter`) ahead of `UsernamePasswordAuthenticationFilter`. Route and method-level authorization (`@EnableMethodSecurity` and `@PreAuthorize`) restrict operations to `STUDENT` or `COMPANY` authorities, while controllers and services verify the authenticated principal's email against the target resource to prevent horizontal privilege escalation.
* **Transactional Application Eligibility & Lifecycle Management:** `PlacementService.applyForJob` runs within a `@Transactional` boundary to verify profile completeness (`cgpa != null`), prevent duplicate submissions (enforced via `ApplicationRepository.existsByStudentAndJob` and a composite `@UniqueConstraint` on `user_id` and `job_id`), validate student CGPA against the job's `requiredCgpa` (`IneligibleCGPAException`), decrement available vacancies, and record the application. Companies can review applicants per job and transition application statuses across `APPLIED`, `REVIEWING`, `ACCEPTED`, and `REJECTED`.
* **Redis Read-Through Caching & Eviction:** `RedisConfig` configures JSON serialization (`RedisSerializer.json()`) with a 10-minute Time-To-Live (TTL). `GET /api/jobs` caches job listings under the `jobs` key (`@Cacheable`), while creating or deleting jobs, updating or deleting company profiles, or submitting applications triggers `@CacheEvict(value = "jobs", allEntries = true)` so clients never read stale data.
* **Input Validation & Centralized Exception Handling:** Request DTOs enforce payload constraints via Jakarta Bean Validation (`@Email`, `@Size(min = 8)`, `@NotBlank`, `@NotNull`, `@DecimalMin("0.0")`, `@DecimalMax("10.0")`, `@Min(1)`). `GlobalExceptionHandler` (`@RestControllerAdvice`) intercepts `IneligibleCGPAException`, `ResponseStatusException`, `AccessDeniedException`, and `MethodArgumentNotValidException` to return structured JSON error responses (`timestamp`, `status`, `error`, `message`) with appropriate HTTP status codes (`400`, `401`, `403`, `404`, `409`, `500`).

---

## Project Structure

```text
placement-backend/
├── src/main/java/com/college/placement_backend/
│   ├── config/
│   │   ├── RedisConfig.java              # JSON serialization & 10-min TTL cache config
│   │   └── SwaggerConfig.java            # OpenAPI 3.1 metadata & BearerAuth security scheme
│   ├── controller/
│   │   ├── AuthController.java           # Registration & JWT login endpoints
│   │   ├── CompanyController.java        # Company listing, profile update & cascading delete
│   │   ├── HomeController.java           # Root ("/") redirect to Swagger UI
│   │   ├── JobController.java            # Job CRUD, applicant viewing & status updates
│   │   └── StudentController.java        # Student listing, profile update & job applications
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
│   │   ├── Student.java                  # Student entity (CGPA & eager skills collection)
│   │   ├── Company.java                  # Company entity (industry & location)
│   │   ├── Job.java                      # Job entity linked to Company (@ManyToOne)
│   │   └── Application.java              # Application entity with unique (user_id, job_id) constraint
│   ├── repository/
│   │   ├── UserRepository.java           # User lookup by email
│   │   ├── StudentRepository.java        # Student CRUD operations
│   │   ├── CompanyRepository.java        # Company CRUD operations
│   │   ├── JobRepository.java            # Job CRUD & company-scoped queries
│   │   └── ApplicationRepository.java    # Duplicate checks & student/job application queries
│   ├── security/
│   │   ├── CustomUserDetailsService.java # Bridges UserRepository with Spring Security
│   │   ├── JwtFilter.java                # Per-request Bearer token extraction & validation
│   │   ├── JwtUtil.java                  # HS256 token generation & claim parsing
│   │   └── SecurityConfig.java           # Stateless filter chain & BCrypt encoder
│   ├── service/
│   │   └── PlacementService.java         # Transactional application & status management logic
│   ├── GlobalExceptionHandler.java       # @RestControllerAdvice centralized error formatter
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
| `POST` | `/api/auth/register` | Public | Register a `STUDENT` or `COMPANY` account (returns `userId` and `role`) |
| `POST` | `/api/auth/login` | Public | Authenticate credentials and receive a JWT, `userId`, and `role` |

### Students (`/api/students`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/students` | `COMPANY` | Retrieve all student profiles |
| `PUT` | `/api/students/{id}/profile` | `STUDENT` (Owner) | Update student CGPA (`0.0` to `10.0`) and skills list |
| `POST` | `/api/students/{studentId}/apply/{jobId}` | `STUDENT` (Owner) | Apply for a job posting (decrements vacancy & evicts `jobs` cache) |
| `GET` | `/api/students/{studentId}/applications` | `STUDENT` (Owner) | View all job applications submitted by the student |

### Companies (`/api/companies`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/companies` | `STUDENT` | Retrieve all registered companies |
| `PUT` | `/api/companies/{id}/profile` | `COMPANY` (Owner) | Update company industry and location details (evicts `jobs` cache) |
| `DELETE` | `/api/companies/{id}` | `COMPANY` (Owner) | Delete company account and cascade-delete its jobs and applications |

### Jobs (`/api/jobs`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/jobs` | Authenticated | List all job postings (cached in Redis under `jobs`) |
| `POST` | `/api/jobs` | `COMPANY` (Owner) | Create a new job posting (evicts `jobs` cache) |
| `GET` | `/api/jobs/{jobId}/applications` | `COMPANY` (Owner) | Retrieve all student applications for a specific job |
| `PUT` | `/api/jobs/applications/{applicationId}/status` | `COMPANY` (Owner) | Update application status via `?status=` (`APPLIED`, `REVIEWING`, `ACCEPTED`, `REJECTED`) |
| `DELETE` | `/api/jobs/{id}` | `COMPANY` (Owner) | Delete a job posting and its associated applications (evicts `jobs` cache) |

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
*Click **Authorize** in Swagger UI and paste the returned `token`, or include it in the `Authorization` header as `Bearer <token>`.*

**3. Update Student Profile (`PUT /api/students/{id}/profile`)**
```json
{
  "cgpa": 8.6,
  "skills": ["Java", "Spring Boot", "MySQL", "Redis"]
}
```

**4. Update Company Profile (`PUT /api/companies/{id}/profile`)**
```json
{
  "industry": "Cloud Computing",
  "location": "Bengaluru"
}
```

**5. Create Job Posting (`POST /api/jobs`)**
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
