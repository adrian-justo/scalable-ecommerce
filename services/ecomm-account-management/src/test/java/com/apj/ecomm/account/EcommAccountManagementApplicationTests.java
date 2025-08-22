package com.apj.ecomm.account;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
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
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.client.cart.BuyerCartResponse;
import com.apj.ecomm.account.web.client.cart.CartItemCatalog;
import com.apj.ecomm.account.web.client.product.ProductCatalog;
import com.apj.ecomm.account.web.client.product.ProductResponse;
import com.apj.ecomm.account.web.messaging.CreateCartEvent;
import com.apj.ecomm.account.web.messaging.ShopNameUpdatedEvent;
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
	void activateIfExistsOk() throws JsonProcessingException {
		final var create = new CreateUserRequest(username.replace("/", ""), "seller123@mail.com", "+639031234567",
				"sellerP@ss123", "Seller Name", "Seller Shop", Set.of(Role.SELLER), Set.of(NotificationType.EMAIL));
		given().contentType("application/json")
			.body(mapper.writeValueAsString(create))
			.when()
			.post(apiVersion + authPath + "register")
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED);

		given().when().delete(apiVersion + usersPath + username).then().assertThat().statusCode(HttpStatus.NO_CONTENT);
		given().when().get(apiVersion + usersPath + username).then().assertThat().statusCode(HttpStatus.NOT_FOUND);

		given().contentType("application/json")
			.body(mapper.writeValueAsString(create))
			.when()
			.post(apiVersion + authPath + "register")
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED);
	}

	@Test
	void clientEstablished() throws JsonProcessingException, JSONException {
		final var userId = "SHP001";
		final List<ProductCatalog> catalog = List.of(new ProductCatalog(1L, "image1", "Item 1", BigDecimal.ONE),
				new ProductCatalog(2L, "image2", "Item 2", BigDecimal.TWO));
		final var result = new Paged<>(catalog, 0, 10, 1, List.of(), catalog.size());
		final var jsonCatalog = mapper.writeValueAsString(result);
		clientMock.stubFor(
				get(urlMatching(apiVersion + productsPath + "?.*")).willReturn(aResponse().withStatus(HttpStatus.OK)
					.withHeader("Content-Type", "application/json")
					.withBody(jsonCatalog)));

		final var catalogResponse = given().header(AppConstants.HEADER_USER_ID, userId)
			.when()
			.get(apiVersion + usersPath + username + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(jsonCatalog, catalogResponse, true);

		final var product = new ProductResponse(1L, "Item 1", userId, "Shop 1", "Description 1", List.of("image1"),
				Set.of("category1"), 1, BigDecimal.ONE);
		final var jsonProduct = mapper.writeValueAsString(product);
		clientMock.stubFor(get(urlMatching(apiVersion + productsPath + "/([0-9]*)"))
			.willReturn(aResponse().withStatus(HttpStatus.OK)
				.withHeader("Content-Type", "application/json")
				.withBody(jsonProduct)));

		final var productResponse = given().header(AppConstants.HEADER_USER_ID, userId)
			.when()
			.get(apiVersion + usersPath + username + productsPath + "/1")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(jsonProduct, productResponse, true);

		final var items = catalog.stream().map(c -> new CartItemCatalog(c, 1)).toList();
		final var cart = new BuyerCartResponse(1L, userId, items, false);
		final var jsonCart = mapper.writeValueAsString(cart);
		clientMock.stubFor(
				get(urlMatching(apiVersion + cartsPath + "/buyer")).willReturn(aResponse().withStatus(HttpStatus.OK)
					.withHeader("Content-Type", "application/json")
					.withBody(jsonCart)));

		final var cartResponse = given().header(AppConstants.HEADER_USER_ID, userId)
			.when()
			.get(apiVersion + usersPath + username + cartsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(jsonCart, cartResponse, true);
	}

	@Test
	void asyncMessageSent() throws IOException {
		final var update = new UpdateUserRequest(null, null, null, null, "Updated Shop Name", null, null, null);

		final var response = given().contentType("application/json")
			.body(update)
			.when()
			.put(apiVersion + usersPath + "/seller001")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		var data = output.receive(100, "product-sync-shop-name");

		final var user = mapper.readValue(response, UserResponse.class);
		final var nameUpdate = mapper.readValue(data.getPayload(), ShopNameUpdatedEvent.class);
		assertNotNull(nameUpdate.shopId());
		assertEquals(user.shopName(), nameUpdate.shopName());

		final var login = new LoginRequest("+639087654321", "#buyeR01");

		given().contentType("application/json")
			.body(login)
			.when()
			.post(apiVersion + authPath + "login")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK);
		data = output.receive(100, "cart-create-if-not-exist");

		final var createCart = mapper.readValue(data.getPayload(), CreateCartEvent.class);
		assertNotNull(createCart.buyerId());
	}

}
