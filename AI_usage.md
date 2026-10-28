# AI Usage — Backend
This document discloses the areas of the JustEat backend where AI assistance (Claude, Anthropic) was used during development.

## Areas Where AI Was Used

### 1. Debugging Errors
AI was used to analyze and troubleshoot errors encountered during backend development, including:
- Compilation and runtime errors.
- Spring Boot and Hibernate errors.
- Database-related errors.
- API errors and unexpected responses.
- JPA-related issues.
- Authentication and authorization errors.
- Configuration issues.
AI assistance was used to identify possible causes and suggest fixes, which were then tested and verified by the developer.

### 2. Dockerfile and Docker Image
AI was used to help with Docker-related implementation, including:
- Creating and refining the backend `Dockerfile`.
- Understanding the Docker build process.
- Building the backend Docker image.
- Tagging the Docker image.
- Running the application inside a Docker container.
- Troubleshooting Docker build and runtime errors.
- Preparing the Docker image for Azure deployment.

### 3. Test Cases
AI was used to help create and troubleshoot backend test cases, including:
- Generating unit-test scenarios.
- Writing JUnit 5 test cases.
- Using Mockito for mocking dependencies.
- Identifying edge cases that should be tested.
- Debugging failing tests.
- Improving test coverage for selected service-layer functionality.
The generated tests were reviewed and executed by the developer.

### 4. Maven Dependencies
AI was used to provide guidance and implementation support for Maven dependencies, including:
- Identifying dependencies required for specific backend functionality.
- Explaining the purpose of Spring Boot, Spring Security, JPA, validation, JWT, testing, and related dependencies.
- Troubleshooting dependency-related compilation and runtime errors.
- Helping resolve dependency configuration issues.

### 5. Security Configuration
AI was used to help implement and troubleshoot parts of the Spring Security configuration, including:
- `SecurityConfig`.
- JWT authentication filter configuration.
- `AuthenticationManager` configuration.
- Password encoding using BCrypt.
- Stateless session configuration.
- CORS configuration.
- Authentication entry-point handling.
- Access-denied handling.
- Spring Security filter-chain configuration.
- Understanding JWT filter placement in the Spring Security filter chain.

### 6. Role-Based Authorization Flow
AI was used to help implement and troubleshoot the role-based authorization flow, including:
- Understanding authentication versus authorization.
- Configuring role-based access using Spring Security.
- Using `@PreAuthorize`.
- Configuring access for `CUSTOMER` and `RESTAURANT_OWNER` roles.
- Troubleshooting unauthorized and forbidden responses.
- Understanding how roles are evaluated for protected endpoints.

### 7. Complex Functionalities
AI was used selectively to help implement or troubleshoot some complex backend functionalities, including:
- JWT access-token and refresh-token flow.
- Password-reset functionality.
- Order status transition logic.
- Cart business rules.
- JPA entity relationships and related persistence issues.
- Service-layer validation and authorization checks.
- Troubleshooting complex interactions between backend components.

## Developer Implementation
The overall backend logic and structure were implemented by the developer.
This includes the overall architecture, database design, entity structure, API structure, business requirements, service logic, controller structure, repository structure, and integration of the application's features.
AI assistance was primarily used for the specific areas listed above: debugging errors, Dockerfile and Docker image work, test cases, dependencies, security configuration, role-based authorization, and selected complex functionalities.
All AI-generated code and suggestions were reviewed, modified where necessary, and tested by the developer before being used in the project.