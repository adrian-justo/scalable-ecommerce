package com.apj.ecomm.account;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.support.MessageBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.client.cart.CartDetailResponse;
import com.apj.ecomm.account.web.client.cart.CartItemDetail;
import com.apj.ecomm.account.web.client.product.ProductResponse;
import com.apj.ecomm.account.web.messaging.AccountInformationDetails;
import com.apj.ecomm.account.web.messaging.CreateCartEvent;
import com.apj.ecomm.account.web.messaging.RequestAccountInformationEvent;
import com.apj.ecomm.account.web.messaging.ShopNameUpdatedEvent;
import com.apj.ecomm.account.web.messaging.ShopStatusUpdatedEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.restassured.RestAssured;

@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommAccountManagementApplicationTests {

	@Value("${api.version}")
	private String apiVersion;

	@Value("${users.path}")
	private String usersPath;

	@Value("${auth.path}")
	private String authPath;

	@Value("${products.path}")
	private String productsPath;

	@Value("${carts.path}")
	private String cartsPath;

	@Value("${orders.path}")
	private String ordersPath;

	@Value("${shop.path}")
	private String shopPath;

	private final String username = "/seller123";

	@LocalServerPort
	private int port;

	@RegisterExtension
	private final WireMockExtension clientMock = WireMockExtension.newInstance()
		.options(WireMockConfiguration.wireMockConfig().port(TestcontainersConfiguration.API_GATEWAY_PORT))
		.build();

	@Autowired
	private PostgreSQLContainer<?> postgres;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private OutputDestination output;

	@Autowired
	private InputDestination input;

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
		assertTrue(postgres.isRunning());
	}

	@Test
	void createAccount_delete_reactivate() throws IOException {
		// 1. Create Account
		final var create = mapper.writeValueAsString(new CreateUserRequest(username.replace("/", ""),
				"seller123@mail.com", "+639031234567", "sellerP@ss123", "Seller Name", "Seller Shop",
				"Seller's Address", Set.of(Role.SELLER), Set.of(NotificationType.EMAIL)));
		given().contentType("application/json")
			.body(create)
			.when()
			.post(apiVersion + authPath + "register")
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED);

		// 2. Check existing orders if seller
		clientMock.stubFor(get(urlEqualTo(apiVersion + shopPath + ordersPath + "/exists"))
			.willReturn(aResponse().withStatus(HttpStatus.OK)
				.withHeader("Content-Type", "application/json")
				.withBody(mapper.writeValueAsString(false))));
		given().when().delete(apiVersion + usersPath + username).then().assertThat().statusCode(HttpStatus.NO_CONTENT);

		// 3. Deactivate products of seller and verify
		var data = output.receive(100, "product-sync-shop-status");
		var details = mapper.readValue(data.getPayload(), ShopStatusUpdatedEvent.class);
		assertFalse(details.active());

		// 4. Verify account deletion
		final var response = given().header(AppConstants.HEADER_USER_ID, "")
			.when()
			.get(apiVersion + usersPath + username)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		final var user = mapper.readValue(response, UserResponse.class);
		assertFalse(user.active());

		// 5. Reactivate account
		given().contentType("application/json")
			.body(create)
			.when()
			.post(apiVersion + authPath + "register")
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED);

		// 6. Reactivate products of seller and verify
		data = output.receive(100, "product-sync-shop-status");
		details = mapper.readValue(data.getPayload(), ShopStatusUpdatedEvent.class);
		assertTrue(details.active());

		// 7. Verify account reactivation
		given().header(AppConstants.HEADER_USER_ID, user.id().toString())
			.when()
			.get(apiVersion + usersPath + username)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK);

	}

	@Test
	void client_getAllProducts() throws JSONException, JsonProcessingException {
		final var userId = "SHP001";
		final var clientResponse = mapper.writeValueAsString(new Paged<>(new PageImpl<>(List.of(
				new ProductResponse(1L, "Item 1", userId, "Shop 1", "Desc 1", List.of(), Set.of(), 1, BigDecimal.ONE),
				new ProductResponse(2L, "Item 2", userId, "Shop 2", "Desc 2", List.of(), Set.of(), 2,
						BigDecimal.TWO)))));
		clientMock.stubFor(
				get(urlMatching(apiVersion + productsPath + "?.*")).willReturn(aResponse().withStatus(HttpStatus.OK)
					.withHeader("Content-Type", "application/json")
					.withBody(clientResponse)));

		final var response = given().header(AppConstants.HEADER_USER_ID, userId)
			.when()
			.get(apiVersion + usersPath + username + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(clientResponse, response, true);
	}

	@Test
	void client_getProductById() throws JSONException, JsonProcessingException {
		final var userId = "SHP001";
		final var clientResponse = mapper.writeValueAsString(
				new ProductResponse(1L, "Item 1", userId, "Shop 1", "Desc 1", List.of(), Set.of(), 1, BigDecimal.ONE));
		clientMock.stubFor(get(urlMatching(apiVersion + productsPath + "/([0-9]*)"))
			.willReturn(aResponse().withStatus(HttpStatus.OK)
				.withHeader("Content-Type", "application/json")
				.withBody(clientResponse)));

		final var response = given().header(AppConstants.HEADER_USER_ID, userId)
			.when()
			.get(apiVersion + usersPath + username + productsPath + "/1")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(clientResponse, response, true);
	}

	@Test
	void client_getCartOfBuyer() throws JSONException, JsonProcessingException {
		final var userId = "SHP001";
		final var clientResponse = mapper.writeValueAsString(new CartDetailResponse(List.of(new CartItemDetail(
				new ProductResponse(1L, "Item 1", userId, "Shop 1", "Desc 1", List.of(), Set.of(), 1, BigDecimal.ONE),
				1))));
		clientMock.stubFor(get(urlMatching(apiVersion + cartsPath)).willReturn(aResponse().withStatus(HttpStatus.OK)
			.withHeader("Content-Type", "application/json")
			.withBody(clientResponse)));

		final var response = given().header(AppConstants.HEADER_USER_ID, userId)
			.when()
			.get(apiVersion + usersPath + username + cartsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(clientResponse, response, true);
	}

	@Test
	void event_syncShopName() throws IOException {
		final var response = given().contentType("application/json")
			.body(new UpdateUserRequest(null, null, null, null, "Updated Shop Name", null, null, null))
			.when()
			.put(apiVersion + usersPath + "/seller001")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		final var user = mapper.readValue(response, UserResponse.class);

		final var data = output.receive(100, "product-sync-shop-name");
		final var event = mapper.readValue(data.getPayload(), ShopNameUpdatedEvent.class);
		assertNotNull(event.shopId());
		assertEquals(user.shopName(), event.shopName());
	}

	@Test
	void event_createIfNotExist() throws IOException {
		given().contentType("application/json")
			.body(new LoginRequest("+639087654321", "#buyeR01"))
			.when()
			.post(apiVersion + authPath + "login")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK);
		final var data = output.receive(100, "cart-create-if-not-exist");

		final var event = mapper.readValue(data.getPayload(), CreateCartEvent.class);
		assertNotNull(event.buyerId());
	}

	@Test
	void message_returnUserDetails() throws IOException {
		final var response = given().header(AppConstants.HEADER_USER_ID, "")
			.when()
			.get(apiVersion + usersPath + "/seller001")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		final var user = mapper.readValue(response, UserResponse.class);
		input.send(MessageBuilder.withPayload(new RequestAccountInformationEvent("", Set.of(user.id()))).build(),
				"account-request-user-details");

		final var data = output.receive(100, "order-return-user-details");
		final var details = mapper.readValue(data.getPayload(), AccountInformationDetails.class);
		assertEquals(details.users().get(user.id()).name(), user.name());
	}

}
