# Scalable E-Commerce Platform
Requirements for this project can be found on this [link](https://roadmap.sh/projects/scalable-ecommerce-platform).

## Pre-requisites
- Docker & Docker compose must be installed.
  ```docker
  docker --version
  docker compose version
  ```
- Docker Network must be created
  ```docker
  docker network create ecomm-net
  ```
- Secret key must be configured for JWT signing.
  - Navigate to `TokenServiceTest.java`.  
    Directory: services/ecomm-account-management/src/test/java/com/apj/ecomm/account/domain/
  - Run class as `JUnit Test` in your IDE.  
    <img width="573" height="45" alt="image" src="https://github.com/user-attachments/assets/ac326aa7-e2a9-4534-9a78-8229ad5edfd1" />
  - Copy the generated key and provide as `SECRET_KEY` in `microservice.yml`.  
    Directory: deployment/
    ```yaml
    SECRET_KEY: generatedKey
    ```

## Deployment
- Navigate to `deployment` directory and run command:
  ```docker
  cd deployment
  docker compose up -d
  ```
This will build images using the `Dockerfile` provided for each microservice. It will start the Config Server but the microservices will not rely on this and use their own `application.yml`.

## Optional Setup
To change the values provided in docker compose files:
- Create `.env` file
  ```properties
  ANY_VAR=defaultValue
  ```
  or directly modify the default values
  ```yaml
  ANY_ENV: ${ANY_VAR:-defaultValue}
  ```

To utilize the config server:
- Create a GitHub repository and modify the ff. environment variables of the Config Server in `microservice.yml` or provide them in `.env`.
  Directory: deployment/use-config/
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
- In docker terminal, navigate to `use-config` directory and run command:
  ```docker
  cd deployment/use-config
  docker compose up -d
  ```

## Github Actions Setup
- Add `DOCKER_USERNAME` & `DOCKER_PASSWORD` to GitHub secrets to push the image to Docker Hub.
