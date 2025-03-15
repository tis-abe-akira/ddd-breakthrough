# Syndicated Loan Management System - Project Guidelines

## Build Commands
- Build project: `cd backend && ./mvnw clean install`
- Run application: `cd backend && ./mvnw spring-boot:run`
- Run all tests: `cd backend && ./mvnw test`
- Run single test: `cd backend && ./mvnw test -Dtest=ClassNameTest#methodName`
- Generate test coverage: `cd backend && ./mvnw test jacoco:report`

## Code Style Guidelines
- Use Spring Boot & JPA/Hibernate conventions
- **Naming**: camelCase for methods/variables, PascalCase for classes, snake_case for DB columns
- **Entities**: Use JPA annotations, include @Version for optimistic locking
- **Services**: Implement AbstractBaseService for common CRUD, add @Transactional
- **DTOs**: Use Lombok annotations (@Getter, @Setter, Builder pattern)
- **Testing**: Use AssertJ assertions, TestDataBuilder for setup, clean up after tests
- **Error Handling**: Throw/catch BusinessException for domain errors
- **Documentation**: Add Javadoc for public methods, especially in service interfaces

## Domain-Driven Design Principles
- Maintain clear boundaries between bounded contexts
- Use value objects for immutable concepts
- Repository pattern for data access
- Service layer for business logic