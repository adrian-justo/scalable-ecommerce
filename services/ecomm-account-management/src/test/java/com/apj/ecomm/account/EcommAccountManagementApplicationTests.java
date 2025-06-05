package com.apj.ecomm.account;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.TokenService;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.RestAssured;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EcommAccountManagementApplicationTests {

	private final String apiVersion = "/api/v1/";

	private final String userServicePath = "users/";

	private final String username = "seller123";

	private static String jwt = "Bearer ";

	@LocalServerPort
	private int port;

	@Autowired
	private PostgreSQLContainer<?> postgres;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private TokenService token;

	@BeforeAll
	static void setUpBeforeClass() {
		RestAssured.baseURI = "http://localhost";
	}

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	@Test
	void connectionEstablished() {
		assertTrue(postgres.isCreated());
		assertTrue(postgres.isRunning());
	}

	@Test
	void endToEnd() throws JsonProcessingException {
		userRegistration();
		userRegistration_alreadyRegistered();
		login();
		accountDetails();
		accountManagement();
		accountDeletion();
		userRegistration_inactiveUser();
	}

	void userRegistration() throws JsonProcessingException {
		CreateUserRequest request = new CreateUserRequest(username, "seller123@mail.com", "+639031234567",
				"sellerP@ss123", "Seller Name", List.of(Role.SELLER));

		String response = given().contentType("application/json").body(mapper.writeValueAsString(request)).when()
				.post(apiVersion + "auth/register").then().assertThat().statusCode(HttpStatus.CREATED).extract().body()
				.asString();
		UserResponse user = mapper.readValue(response, new TypeReference<UserResponse>() {
		});

		assertEquals(request.roles(), user.roles());
		assertTrue(user.active());
		assertTrue(encoder.matches(request.password(), user.password()));
	}

	void userRegistration_alreadyRegistered() throws JsonProcessingException {
		CreateUserRequest request = new CreateUserRequest("nonexistent", "seller123@mail.com", "+639031234567",
				"sellerP@ss123", "Seller Name", List.of(Role.SELLER));
		given().contentType("application/json").body(mapper.writeValueAsString(request)).when()
				.post(apiVersion + "auth/register").then().assertThat().statusCode(HttpStatus.CONFLICT);
	}

	void login() {
		LoginRequest request = new LoginRequest(username, "sellerP@ss123");

		String response = given().contentType("application/json").body(request).when().post(apiVersion + "auth/login")
				.then().assertThat().statusCode(HttpStatus.OK).extract().body().asString();

		jwt += response.replace("\"", "");
		assertTrue(token.isValid(jwt.replace("Bearer ", "")));
	}

	void accountDetails() throws JsonProcessingException {
		String response = given().header(HttpHeaders.AUTHORIZATION, jwt).when()
				.get(apiVersion + userServicePath + username).then().assertThat().statusCode(HttpStatus.OK).extract()
				.body().asString();
		UserResponse user = mapper.readValue(response, new TypeReference<UserResponse>() {
		});

		assertEquals("seller123@mail.com", user.email());
		assertEquals(List.of(NotificationType.EMAIL), user.notificationTypes());
	}

	void accountManagement() throws JsonProcessingException {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639041234567", null, null, null, null,
				null, null);

		String response = given().header(HttpHeaders.AUTHORIZATION, jwt).contentType("application/json").body(request)
				.when().put(apiVersion + userServicePath + username).then().assertThat().statusCode(HttpStatus.OK)
				.extract().body().asString();
		UserResponse user = mapper.readValue(response, new TypeReference<UserResponse>() {
		});

		assertEquals(username, user.username());
		assertEquals(request.email(), user.email());
		assertEquals(request.mobileNo(), user.mobileNo());
		assertTrue(encoder.matches("sellerP@ss123", user.password()));
	}

	void accountDeletion() {
		given().header(HttpHeaders.AUTHORIZATION, jwt).when().delete(apiVersion + userServicePath + username).then()
				.assertThat().statusCode(HttpStatus.NO_CONTENT);
		given().header(HttpHeaders.AUTHORIZATION, jwt).when().get(apiVersion + userServicePath + username).then()
				.assertThat().statusCode(HttpStatus.NOT_FOUND);
	}

	void userRegistration_inactiveUser() throws JsonProcessingException {
		userRegistration();
	}

	@Test
	void login_incorrectCredentials() {
		LoginRequest request = new LoginRequest(username, "wrongPassword");
		given().contentType("application/json").body(request).when().post(apiVersion + "auth/login").then().assertThat()
				.statusCode(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void accountDetails_notFound() {
		given().header(HttpHeaders.AUTHORIZATION, jwt).when().get(apiVersion + userServicePath + "nonexistent").then()
				.assertThat().statusCode(HttpStatus.NOT_FOUND);
	}

	@Test
	void accountManagement_invalidDetails() {
		UpdateUserRequest request = new UpdateUserRequest("", "", null, null, null, null, null, null);
		given().header(HttpHeaders.AUTHORIZATION, jwt).contentType("application/json").body(request).when()
				.put(apiVersion + userServicePath + username).then().assertThat().statusCode(HttpStatus.BAD_REQUEST);
	}

}
