package com.apj.ecomm.cart;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.cart.constants.AppConstants;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.client.product.ProductResponse;
import com.apj.ecomm.cart.web.messaging.CreateCartEvent;
import com.apj.ecomm.cart.web.messaging.UpdateCartItemsEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.restassured.RestAssured;

@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommShoppingCartApplicationTests {

	private final String shopId = "SHP002";

	@Value("${api.version}")
	private String apiVersion;

	@Value("${admin.path}")
	private String adminPath;

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
	void setUp() throws JsonProcessingException {
		RestAssured.port = port;
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		final var products = new Paged<>(new PageImpl<>(List.of(
				new ProductResponse(1L, "Item 1", "client123", "Shop 1", "Description 1", List.of("image1"),
						Set.of("category1"), 1, BigDecimal.ONE),
				new ProductResponse(2L, "Item 2", shopId, "Shop 2", "Description 2", List.of("image2"),
						Set.of("category2"), 0, BigDecimal.TWO),
				new ProductResponse(3L, "Item 3", shopId, "Shop 3", "Description 3", List.of("image3"),
						Set.of("category3"), 2, BigDecimal.ONE))));
		clientMock.stubFor(
				get(urlMatching(apiVersion + productsPath + "\\?.*")).willReturn(aResponse().withStatus(HttpStatus.OK)
					.withHeader("Content-Type", "application/json")
					.withBody(mapper.writeValueAsString(products))));
	}

	@Test
	void connectionEstablished() {
		assertTrue(postgres.isRunning());
	}

	@Test
	void add_update_remove_cartItems() throws JsonProcessingException, JSONException {
		final var buyerId = "client123";
		var response = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType("application/json")
			.body(List.of(new CartItemRequest(1L), new CartItemRequest(3L), new CartItemRequest(5L)))
			.when()
			.post(apiVersion + path + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED)
			.extract()
			.body()
			.asString();
		assertEquals(mapper.writeValueAsString(List.of(new CartItemResponse(1L, 3L, shopId, 2, null, null))), response,
				true);

		response = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType("application/json")
			.body(List.of(new CartItemRequest(2L), new CartItemRequest(3L, 3), new CartItemRequest(4L)))
			.when()
			.put(apiVersion + path + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		assertEquals(mapper.writeValueAsString(List.of(new CartItemResponse(1L, 3L, shopId, 2, null, null))), response,
				true);
		assertFalse(response.contains("image2")); // Removed as out of stock

		given().header(AppConstants.HEADER_USER_ID, buyerId)
			.queryParam("id", 3, 4)
			.when()
			.delete(apiVersion + path + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.NO_CONTENT);
		given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + productsPath + "/3")
			.then()
			.assertThat()
			.statusCode(HttpStatus.NOT_FOUND);
	}

	@Test
	void message_createIfNotExist() {
		final var buyerId = "newBuyer";
		given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.NOT_FOUND);

		input.send(MessageBuilder.withPayload(new CreateCartEvent(buyerId)).build(), "cart-create-if-not-exist");

		given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK);
	}

	@Test
	void message_updateCartItems() throws IOException {
		final var buyerId = "buyer001";
		final var response = when().get(apiVersion + adminPath + path + "/2")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		final var cart = mapper.readValue(response, CartResponse.class);
		final var item = cart.products().getFirst();

		input.send(MessageBuilder
			.withPayload(new UpdateCartItemsEvent(buyerId, Map.of(item.productId(), item.quantity() - item.quantity())))
			.build(), "cart-update-item-quantity");
		final var itemResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + productsPath)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		final var items = mapper.readValue(itemResponse, new TypeReference<List<CartItemResponse>>() {
		});

		assertFalse(items.contains(item));
	}

}
