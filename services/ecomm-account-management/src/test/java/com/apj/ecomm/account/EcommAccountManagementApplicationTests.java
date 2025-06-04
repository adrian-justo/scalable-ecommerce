package com.apj.ecomm.account;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
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

	@LocalServerPort
	private int port;

	@Autowired
	private PostgreSQLContainer<?> postgres;

	@Autowired
	private ObjectMapper mapper;

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
	@Order(1)
	void connectionEstablished() {
		assertTrue(postgres.isCreated());
		assertTrue(postgres.isRunning());
	}

	@Test
	@Order(2)
	void userRegistration() throws JacksonException {
		CreateUserRequest request = new CreateUserRequest(username, "seller123@mail.com", "+639031234567",
				"sellerP@ss123", "Seller Name", List.of(Role.SELLER));

		String response = given().contentType("application/json").body(mapper.writeValueAsString(request)).when()
				.post(apiVersion + "auth/register").then().assertThat().statusCode(HttpStatus.CREATED).extract().body()
				.asString();
		UserResponse user = mapper.readValue(response, new TypeReference<UserResponse>() {
		});

		assertEquals(request.roles(), user.roles());
		assertTrue(user.active());
	}

	@Test
	@Order(3)
	void login() {
		LoginRequest request = new LoginRequest(username, "sellerP@ss123");

		String response = given().contentType("application/json").body(request).when().post(apiVersion + "auth/login")
				.then().assertThat().statusCode(HttpStatus.OK).extract().body().asString();

		assertFalse(response.isEmpty());
	}

	@Test
	@Order(4)
	void accountDetails() throws JacksonException {
		String response = given().header(HttpHeaders.AUTHORIZATION, "Bearer test").when()
				.get(apiVersion + userServicePath + username).then().assertThat().statusCode(HttpStatus.OK).extract()
				.body().asString();
		UserResponse user = mapper.readValue(response, new TypeReference<UserResponse>() {
		});

		assertEquals("seller123@mail.com", user.email());
		assertEquals(List.of(NotificationType.EMAIL), user.notificationTypes());
	}

	@Test
	@Order(5)
	void accountManagement() throws JacksonException {
		UpdateUserRequest request = new UpdateUserRequest("updated@email.com", "+639041234567", null, null, null, null,
				null, null);

		String response = given().header(HttpHeaders.AUTHORIZATION, "Bearer test").contentType("application/json")
				.body(request).when().put(apiVersion + userServicePath + username).then().assertThat()
				.statusCode(HttpStatus.OK).extract().body().asString();
		UserResponse user = mapper.readValue(response, new TypeReference<UserResponse>() {
		});

		assertEquals(username, user.username());
		assertEquals(request.email(), user.email());
		assertEquals(request.mobileNo(), user.mobileNo());
		assertNotNull(user.password());
	}

	@Test
	@Order(6)
	void accountDeletion() {
		given().header(HttpHeaders.AUTHORIZATION, "Bearer test").when()
				.delete(apiVersion + userServicePath + username).then().assertThat()
				.statusCode(HttpStatus.NO_CONTENT);
		given().header(HttpHeaders.AUTHORIZATION, "Bearer test").when().get(apiVersion + userServicePath + username)
				.then().assertThat().statusCode(HttpStatus.NOT_FOUND);
	}

	@Test
	@Order(7)
	void userRegistration_inactiveUser() throws JacksonException {
		userRegistration();
	}

}
