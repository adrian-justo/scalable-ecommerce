package com.apj.ecomm.product;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.math.BigDecimal;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;

import io.restassured.RestAssured;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommProductCatalogApplicationTests {

	private final String apiVersion = "/api/v1/";

	private final String path = "products";

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
		String getResponse = when().get(apiVersion + path + "?page=0&size=2"
				+ "&filter=categories:perfumes|(price<10;stock>1)|createdAt:2025/07/08T00:00:00Z-2025/07/08T23:59:59Z"
				+ "&sort=price,desc&sort=stock").then().assertThat().statusCode(HttpStatus.OK).extract().body()
				.asString();
		assertEquals(getResponse, redisTemplate.keys("catalog::*").stream()
				.map(key -> redisTemplate.opsForValue().get(key)).toList().getFirst(), true);

		CreateProductRequest createRequest = new CreateProductRequest("name", null, null, null, null, null);
		String createdResponse = given().header(AppConstants.HEADER_SHOP_NAME, "Shop Name")
				.contentType("application/json").body(mapper.writeValueAsString(createRequest)).when()
				.post(apiVersion + path).then().assertThat().statusCode(HttpStatus.CREATED).extract().body().asString();
		ProductResponse created = mapper.readValue(createdResponse, ProductResponse.class);

		String cacheResponse = when().get(apiVersion + path + '/' + created.id()).then().assertThat()
				.statusCode(HttpStatus.OK).extract().body().asString();
		assertEquals(createdResponse, cacheResponse, true);

		UpdateProductRequest updateRequest = new UpdateProductRequest(null, "description", null, null, 0,
				BigDecimal.ONE);
		String updatedResponse = given().contentType("application/json").body(mapper.writeValueAsString(updateRequest))
				.when().put(apiVersion + path + '/' + created.id()).then().assertThat().statusCode(HttpStatus.OK)
				.extract().body().asString();

		cacheResponse = when().get(apiVersion + path + '/' + created.id()).then().assertThat().statusCode(HttpStatus.OK)
				.extract().body().asString();
		assertEquals(updatedResponse, cacheResponse, true);
	}

}
