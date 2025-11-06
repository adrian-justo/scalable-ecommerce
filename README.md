# Scalable E-Commerce Platform
This project showcases a fully realized microservices architecture built using Spring Boot, Spring Cloud, and cutting-edge cloud-native technologies. It powers several key functions of an e-commerce platform, including user authentication, product management, and order fulfillment. The application exposes RESTful APIs secured with JSON web token (JWT) authentication, integrates Stripe for seamless payment processing, and leverages Twilio for real-time user notifications.

Requirements for this project can be found on this [link](https://roadmap.sh/projects/scalable-ecommerce-platform).

## Architecture
- **API Gateway**: Serves as the single entry point, ensuring resiliency, rate limiting, and centralized authorization for all incoming requests.
- **Service Registry**: Utilizes Spring Cloud Netflix for dynamic service discovery and automated load balancing across microservices.
- **Centralized Configuration Management**: Managed by Spring Cloud Config, enabling consistent configuration across all services.
- **Inter-Service Communication**: Supports synchronous communication via Spring Cloud OpenFeign and asynchronous messaging through Spring Cloud Stream for event-driven interactions.
- **Authentication & Authorization**: Secured with Spring Security and JWT, providing robust, token-based authentication and fine-grained authorization.
- **Multi-Tenancy**: Implements role-based access control (RBAC) for managing access across multiple tenants within the system.
- **Eventual Consistency**: Achieved through a choreography-based saga pattern, ensuring consistency across services while handling distributed transactions.
- **Distributed Locking**: Leveraging Spring Integration and Redis to provide distributed locks, ensuring safe resource access across services.
- **Monitoring & Observability**: Integrated with the Grafana stack, offering comprehensive monitoring, logging, and real-time observability across the microservices ecosystem.
<p align="center"><img width="752" height="790" alt="image" src="https://github.com/user-attachments/assets/9dbd0170-ed34-4103-afdd-114a6c181232" /></p>

## Tech Stack
- **Framework**: Spring Boot 3.5.3, Spring Cloud 2025.0.0
- **Language**: Java 21
- **Database**: PostgreSQL, Redis (Caching), Flyway (Migration)
- **Message Broker**: RabbitMQ
- **Build Tool**: Maven
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Containerization**: Docker & Docker Compose
- **CI/CD Pipeline**: GitHub Actions
- **Testing**: JUnit, Mockito, WireMock, REST-Assured
- **Boilerplate Code Reduction**: Lombok, MapStruct

## Pre-requisites
- Java 21 or higher must be installed.
  ```bash
  java -version
  ```
- Maven 3.9 or higher must be installed.
  ```bash
  mvn -version
  ```
- Docker & Docker compose must be installed.
  ```docker
  docker --version
  docker compose version
  ```
- Docker Network must be created.
  ```docker
  docker network create ecomm-net
  ```

## Getting Started
- Clone the repository.
  ```bash
  git clone https://github.com/adrian-justo/scalable-ecommerce.git
  cd scalable-ecommerce
  ```
-  Configure secret key for JWT signing.
    - Generate secret key
      ```bash
      cd scalable-ecommerce/services/ecomm-account-management
      mvn test -Dtest=com.apj.ecomm.account.domain.TokenServiceTest.java
      ```
    - Copy the generated key and provide as `SECRET_KEY` in `microservice.yml`.
      ```bash
      cd scalable-ecommerce/deployment
      ```
      ```yaml
      ...
      SECRET_KEY: generatedKey
      ```
- Navigate to `deployment` directory and run command:
  ```bash
  cd scalable-ecommerce/deployment
  docker compose up -d
  ```
This will build images using the `Dockerfile` provided for each microservice. It will start the Config Server but the microservices will not rely on this and use their own `application.yml` configuration.

## Optional Setup
To change the values provided in docker compose files:
- Create `.env` file
  ```properties
  ANY_VAR=defaultValue
  ```
  or directly modify the default values.
  ```yaml
  ANY_ENV: ${ANY_VAR:-defaultValue}
  ```

To utilize the config server:
- Create a GitHub repository and modify the ff. environment variables of the Config Server in `microservice.yml` or provide them in `.env`.
  ```bash
  cd scalable-ecommerce/deployment/use-config
  ```
  ```yaml
  services:
    ...
    config-server:
      ...
      environment:
        ...
        GIT_URI: ${GIT_URI:-https://github.com/user/your-private-repo}
        GIT_USERNAME: ${GIT_USERNAME:-optionalIfPublicRepo}
        GIT_PASSWORD: ${GIT_PASSWORD:-optionalIfPublicRepo}
  ```
  > Make sure you have also provided here the secret key generated earlier.
- Navigate to `use-config` directory and run command:
  ```bash
  cd scalable-ecommerce/deployment/use-config
  docker compose up -d
  ```

## API Documentation
Once the application is running, access the Swagger UI documentation at:
```http
http://localhost:8080/docs
```

## Github Actions Setup
- Add `DOCKER_USERNAME` & `DOCKER_PASSWORD` to GitHub secrets to push the image to Docker Hub.

## Project Structure
```properties
scalable-ecommerce/
├── .github/workflows/  # GitHub Actions for each microservice
├── deployment/         # Docker Compose and configuration files
│   ├── grafana/        # Configuration file
│   ├── prometheus/     # Configuration file
│   ├── promtail/       # Configuration file
│   ├── tempo/          # Configuration file
│   ├── use-config/     # Docker Compose to utilize config server
│   └── use-hub/        # Docker Compose to utilize Docker Hub
├── documents/          # Architectural design, API documentation, etc. 
└── services/           # All microservices
```
```properties
scalable-ecommerce/services/<microservice-directory>/src/main/java/com/apj/ecomm/<microservice>/
├── config/          # Configuration classes (Security, Observability, etc.)
├── constants/       # Validation Messages, RegExp Patterns, etc.
├── domain/          # Encapsulated business logic, entities, and repositories
│   └── model/       # Request and response classes
└── web/             # Classes for handling user interactions and presenting information 
    ├── client/      # Classes related to synchronuous inter-service communication
    ├── controller/  # REST API controllers
    ├── exception/   # Custom exceptions and handling
    ├── messaging/   # Classes related to asynchronuous inter-service communication
    └── util/        # Utility classes
```
