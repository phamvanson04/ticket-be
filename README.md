# :clapper: Cinebee Backend

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8+-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis&logoColor=white)](https://redis.io/)

Backend API for Cinebee movie ticket platform, built with Spring Boot and organized using Clean Architecture layers for easier maintenance and scaling.

## :sparkles: Highlights

- :lock: JWT auth + Google OAuth2 login flow
- :busts_in_silhouette: Role-based access (`USER`, `ADMIN`)
- :ticket: Booking flow with seat lock strategy to reduce double booking risk
- :moneybag: MoMo payment integration (create payment + callback handling)
- :email: Ticket email notification with QR code
- :zap: Redis caching for frequently used data
- :cloud: Cloudinary media upload support
- :building_construction: Clean Architecture package separation
- :test_tube: ArchUnit guardrails to protect architecture boundaries

## :compass: Clean Architecture Layout

```text
src/main/java/com/cinebee
|-- presentation      # Controllers + request/response DTOs
|-- application       # Use-case services, mappers, app utilities
|-- domain            # Core entities and business model
|-- infrastructure    # Config, security, scheduler, persistence adapters
`-- shared            # Cross-cutting utilities, exceptions, enums
```

Main entrypoint: `src/main/java/com/cinebee/CineBeeApplication.java`

## :tools: Tech Stack

- Java 21
- Spring Boot 3.3.3
- Spring Data JPA + Hibernate
- Spring Security + JWT
- MySQL
- Redis
- Cloudinary
- MoMo payment API
- Thymeleaf + Jakarta Mail
- ZXing (QR code)
- Maven

## :rocket: Quick Start

### 1) Prerequisites

- JDK 21
- Maven 3.8+
- MySQL 8+
- Redis 6+

### 2) Clone

```bash
git clone https://github.com/phamvanson04/ticket-be.git
cd ticket-be
```

### 3) Configure environment

Create `.env` in project root:

```env
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password

REDIS_PASSWORD=

MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_key
CLOUDINARY_API_SECRET=your_cloudinary_secret

MOMO_PARTNER_CODE=your_partner_code
MOMO_ACCESS_KEY=your_access_key
MOMO_SECRET_KEY=your_secret_key

RECAPTCHA_SECRET=your_recaptcha_secret
```

### 4) Run dependencies (optional by Docker)

```bash
docker compose up -d
```

### 5) Run application

```bash
mvn clean install
mvn spring-boot:run
```

Default URL: `http://localhost:8080`

## :package: API Modules

Current controllers:

- `AuthController`
- `BannerController`
- `CaptchaController`
- `LoginController`
- `MovieController`
- `PaymentController`
- `ProfileController`
- `RegisterController`
- `ShowtimeController`
- `TheaterController`

## :test_tube: Testing

Run all tests:

```bash
mvn test
```

Architecture tests use ArchUnit and currently enforce:

- domain does not depend on application/presentation/infrastructure
- shared does not depend on domain/application/presentation/infrastructure
- presentation does not depend on infrastructure

## :file_folder: Important Files

- `pom.xml`
- `docker-compose.yml`
- `src/main/resources/application.yml`
- `src/test/java/com/cinebee/architecture/CleanArchitectureRulesTest.java`

## :handshake: Contributing

- Create feature branch from `master`
- Keep commits atomic and message clear (conventional commit style preferred)
- Ensure `mvn test` passes before creating PR

## :page_facing_up: License

Internal/Project-specific usage.
