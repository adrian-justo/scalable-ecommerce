package com.apj.ecomm.product;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.messaging.OrderedProductDetails;
import com.apj.ecomm.product.web.messaging.ProductStockUpdate;
import com.apj.ecomm.product.web.messaging.ReturnProductStockEvent;
import com.apj.ecomm.product.web.messaging.ShopNameUpdatedEvent;
import com.apj.ecomm.product.web.messaging.ShopStatusUpdatedEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;

import io.restassured.RestAssured;

@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommProductCatalogApplicationTests {

	@Value("${api.version}")
	private String apiVersion;

	@Value("${products.path}")
	private String path;

	@LocalServerPort
	private int port;

	@Autowired
	private PostgreSQLContainer<?> postgres;

	@Autowired
	private RedisContainer redis;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private InputDestination input;

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
		assertTrue(redis.isRunning());
	}

	@Test
	void cache_Search() {
		when().get(apiVersion + path + "?page=0&size=2"
				+ "&filter=categories:perfumes|(price<10;stock>1)|createdAt:2025-01-01T00:00:00Z->2025-01-01T23:59:59Z"
				+ "&sort=price,desc&sort=stock")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK);

		assertNotNull(redisTemplate.keys("catalog::*")
			.stream()
			.map(key -> redisTemplate.opsForValue().get(key))
			.toList()
			.getFirst());
	}

	@Test
	void cache_Create() throws JSONException, JsonProcessingException {
		final var response = given().header(AppConstants.HEADER_USER_ID, "SHP001")
			.header(AppConstants.HEADER_SHOP_NAME, "Shop Name")
			.contentType("application/json")
			.body(mapper.writeValueAsString(new CreateProductRequest("name", null, null, null, null, null)))
			.when()
			.post(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED)
			.extract()
			.body()
			.asString();
		final var created = mapper.readValue(response, ProductResponse.class);
		final var cacheResponse = when().get(apiVersion + path + '/' + created.id())
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(response, cacheResponse, true);
	}

	@Test
	void cache_Update() throws JSONException, JsonProcessingException {
		final var productId = "/1";

		final var updatedResponse = given().header(AppConstants.HEADER_USER_ID, "SHP001")
			.contentType("application/json")
			.body(mapper
				.writeValueAsString(new UpdateProductRequest(null, "description", null, null, 0, BigDecimal.ONE)))
			.when()
			.put(apiVersion + path + productId)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		final var cacheResponse = when().get(apiVersion + path + productId)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertEquals(updatedResponse, cacheResponse, true);
	}

	@Test
	void message_syncShopName() throws IOException {
		final var shopName = "Updated Shop Name";

		input.send(MessageBuilder.withPayload(new ShopNameUpdatedEvent("SHP001", shopName)).build(),
				"product-sync-shop-name");
		final var product = getBy(1L);

		assertEquals(shopName, product.shopName());
	}

	@Test
	void message_processAndReturnDetail() throws IOException {
		input.send(MessageBuilder.withPayload(new ProductStockUpdate("", Map.of(3L, 2))).build(),
				"product-update-stock-ordered");
		final var product = getBy(3L);
		final var data = output.receive(100, "order-return-product-details");
		final var ordered = mapper.readValue(data.getPayload(), OrderedProductDetails.class);

		assertEquals(0, product.stock());
		assertEquals(1, ordered.details().get(product.id()).stock());
	}

	@Test
	void message_returnProductStock() throws IOException {
		final var quantity = 2;
		input.send(MessageBuilder.withPayload(new ReturnProductStockEvent(Map.of(3L, quantity))).build(),
				"product-return-stock-ordered");
		final var product = getBy(3L);

		assertEquals(quantity, product.stock());
	}

	@Test
	void message_syncShopStatus() throws IOException {
		final var product = getBy(3L);

		input.send(MessageBuilder.withPayload(new ShopStatusUpdatedEvent(product.shopId(), Boolean.FALSE)).build(),
				"product-sync-shop-status");
		final var response = when().get(apiVersion + path + "?id=3")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertFalse(response.contains(product.shopId()));
	}

	private ProductResponse getBy(final long id) throws JsonProcessingException {
		final var response = when().get(apiVersion + path + '/' + id)
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		return mapper.readValue(response, ProductResponse.class);
	}

}
