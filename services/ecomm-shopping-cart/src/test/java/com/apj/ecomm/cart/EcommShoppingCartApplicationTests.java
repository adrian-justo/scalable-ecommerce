package com.apj.ecomm.cart;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.cart.constants.AppConstants;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.client.product.ProductCatalog;
import com.apj.ecomm.cart.web.client.product.ProductResponse;
import com.apj.ecomm.cart.web.messaging.CreateCartEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.restassured.RestAssured;

@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommShoppingCartApplicationTests {

	@Value("${api.version}")
	private String apiVersion;

	@Value("${carts.path}")
	private String path;

	@Value("${products.path}")
	private String productsPath;

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
	void clientEstablished() throws JsonProcessingException, JSONException {
		final var buyerId = "client123";
		final List<ProductCatalog> catalog = List.of(new ProductCatalog(1L, "image1", "Item 1", BigDecimal.ONE),
				new ProductCatalog(2L, "image2", "Item 2", BigDecimal.TWO),
				new ProductCatalog(3L, "image3", "Item 3", new BigDecimal(0.3)));
		clientMock.stubFor(
				get(urlMatching(apiVersion + productsPath + "?.*")).willReturn(aResponse().withStatus(HttpStatus.OK)
					.withHeader("Content-Type", "application/json")
					.withBody(mapper.writeValueAsString(new Paged<>(catalog, 0, 10, 1, List.of(), catalog.size())))));

		var cartDetailResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + "/buyer")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertTrue(cartDetailResponse.contains("Item 1"));
		assertTrue(cartDetailResponse.contains("image2"));

		final var product = new ProductResponse(1L, "Item 1", "SHP001", "Shop 1", "Description 1", List.of("image1"),
				Set.of("category1"), 1, BigDecimal.ONE);
		clientMock.stubFor(get(urlMatching(apiVersion + productsPath + "/([0-9]*)"))
			.willReturn(aResponse().withStatus(HttpStatus.OK)
				.withHeader("Content-Type", "application/json")
				.withBody(mapper.writeValueAsString(product))));

		final var cartItemDetailResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + "/1" + productsPath + "/1")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertTrue(cartItemDetailResponse.contains("Item 1"));

		final var addToCartResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType("application/json")
			.body(List.of(new CartItemRequest(1L), new CartItemRequest(3L), new CartItemRequest(5L, 2)))
			.when()
			.post(apiVersion + path + "/1" + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED)
			.extract()
			.body()
			.asString();

		final var addToCartExpected = List.of(new CartItemResponse(1L, 3), new CartItemResponse(3L, 1));
		assertEquals(mapper.writeValueAsString(addToCartExpected), addToCartResponse, true);

		final var updateQuantityResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType("application/json")
			.body(List.of(new CartItemRequest(1L), new CartItemRequest(4L)))
			.when()
			.put(apiVersion + path + "/1" + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		final var updateQuantityExpected = List.of(new CartItemResponse(1L, 1));
		assertEquals(mapper.writeValueAsString(updateQuantityExpected), updateQuantityResponse, true);

		given().header(AppConstants.HEADER_USER_ID, buyerId)
			.queryParam("id", 3, 4)
			.when()
			.delete(apiVersion + path + "/1" + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.NO_CONTENT);

		cartDetailResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + "/buyer")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertTrue(cartDetailResponse.contains("Item 1"));
		assertTrue(cartDetailResponse.contains("image2"));
		assertFalse(cartDetailResponse.contains("image3"));
	}

	@Test
	void asyncMessageReceived() throws IOException {
		final var buyerId = "newBuyer";
		final var event = new CreateCartEvent(buyerId);

		input.send(MessageBuilder.withPayload(event).build(), "cart-create-if-not-exist");
		final var response = when().get(apiVersion + path + "/3")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		final var cart = mapper.readValue(response, CartResponse.class);
		assertEquals(buyerId, cart.buyerId());
	}

}
