---
name: Spoony Backend — project structure
description: Real Java package name, key source file paths, and architecture layout for spoony-backend
type: project
---

Real Java package is `com.spoony.backend` (NOT `com.spoony.spoony_backend` as the artifact ID might suggest).

**Key source paths (relative to repo root `spoony-backend/`):**
- `src/main/java/com/spoony/backend/infrastructure/security/JwtAuthenticationFilter.java`
- `src/main/java/com/spoony/backend/infrastructure/security/JwtTokenProvider.java`
- `src/main/java/com/spoony/backend/infrastructure/config/BeanConfig.java` — has `@EnableScheduling`
- `src/main/java/com/spoony/backend/infrastructure/config/SecurityConfig.java`
- `src/main/java/com/spoony/backend/infrastructure/config/DataRetentionScheduler.java`
- `src/main/java/com/spoony/backend/application/auth/AuthService.java`
- `src/main/java/com/spoony/backend/application/rest/user/UserController.java`
- `src/main/java/com/spoony/backend/application/rest/user/UserService.java`
- `src/main/java/com/spoony/backend/application/rest/user/UserExportResponse.java`
- `src/main/java/com/spoony/backend/infrastructure/persistence/entity/UserEntity.java`
- `src/main/java/com/spoony/backend/infrastructure/persistence/repository/JpaUserRepository.java`
- `src/main/resources/application.yml`
- Flyway migrations: `src/main/resources/db/migration/V6__add_last_login_at_to_users.sql`

**Architecture:** Hexagonal (ports & adapters). Domain services wired manually in BeanConfig (not @Service). Infrastructure layer holds JPA entities, repositories, security filters, and config/schedulers.

**Stack:** Spring Boot 3.5.11, Java 21, PostgreSQL, Flyway, JJWT 0.12.5, springdoc 2.3.0.

**Why:** Confirmed via `target/maven-status/maven-compiler-plugin/compile/default-compile/inputFiles.lst`
**How to apply:** Always use `com.spoony.backend.*` when navigating or referencing source files.
