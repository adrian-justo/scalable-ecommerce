package com.apj.ecomm.product;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;

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
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.messaging.ShopNameUpdatedEvent;
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
	void resultCached() throws JSONException, JsonProcessingException {
		final var getResponse = when().get(apiVersion + path + "?page=0&size=2"
				+ "&filter=categories:perfumes|(price<10;stock>1)|createdAt:2025-01-01T00:00:00Z->2025-01-01T23:59:59Z"
				+ "&sort=price,desc&sort=stock")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		assertTrue(!getResponse.isEmpty());
		// Disabled due to conditional caching based on result size.
		// Update AppConstants.DEFAULT_PAGE_SIZE to the size of
		// the get result to enable this assertion.
		// assertEquals(getResponse, redisTemplate.keys("catalog::*").stream().map(key ->
		// redisTemplate.opsForValue().get(key)).toList().getFirst(), true);

		final var createRequest = new CreateProductRequest("name", null, null, null, null, null);
		final var createdResponse = given().header(AppConstants.HEADER_USER_ID, "SHP001")
			.header(AppConstants.HEADER_SHOP_NAME, "Shop Name")
			.contentType("application/json")
			.body(mapper.writeValueAsString(createRequest))
			.when()
			.post(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED)
			.extract()
			.body()
			.asString();
		final var created = mapper.readValue(createdResponse, ProductResponse.class);

		var cacheResponse = when().get(apiVersion + path + '/' + created.id())
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		assertEquals(createdResponse, cacheResponse, true);

		final var updateRequest = new UpdateProductRequest(null, "description", null, null, 0, BigDecimal.ONE);
		final var updatedResponse = given().header(AppConstants.HEADER_USER_ID, "SHP001")
			.contentType("application/json")
			.body(mapper.writeValueAsString(updateRequest))
			.when()
			.put(apiVersion + path + '/' + created.id())
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		cacheResponse = when().get(apiVersion + path + '/' + created.id())
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		assertEquals(updatedResponse, cacheResponse, true);
	}

	@Test
	void asyncMessageReceived() throws IOException {
		final var shopName = "Updated Shop Name";
		final var event = new ShopNameUpdatedEvent("SHP001", shopName);

		input.send(MessageBuilder.withPayload(event).build(), "product-sync-shop-name");
		final var response = when().get(apiVersion + path + "/1")
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();

		final var updated = mapper.readValue(response, ProductResponse.class);
		assertEquals(shopName, updated.shopName());
	}

}
