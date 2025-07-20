package com.apj.ecomm.account;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.spec.internal.HttpStatus;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.model.ProductCatalog;
import com.apj.ecomm.account.domain.model.ProductResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.restassured.RestAssured;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommAccountManagementApplicationTests {

	@LocalServerPort
	private int port;

	@RegisterExtension
	WireMockExtension productServiceMock = WireMockExtension.newInstance()
			.options(WireMockConfiguration.wireMockConfig().port(TestcontainersConfiguration.API_GATEWAY_PORT)).build();

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
	void connectionEstablished() {
		assertTrue(postgres.isRunning());
	}

	@Test
	void clientEstablished() throws JsonProcessingException, JSONException {
		String apiVersion = "/api/v1";
		String productsPath = "/products";
		String usersPath = "/users";
		String username = "/seller123";
		String shopName = "Shop 1";

		List<ProductCatalog> catalog = List.of(new ProductCatalog(1L, "image1", "Item 1", "1"),
				new ProductCatalog(2L, "image2", "Item 2", "2"));
		String jsonCatalog = mapper.writeValueAsString(catalog);
		productServiceMock.stubFor(get(urlMatching(apiVersion + productsPath + "?.*")).willReturn(aResponse()
				.withStatus(HttpStatus.OK).withHeader("Content-Type", "application/json").withBody(jsonCatalog)));

		String catalogResponse = given().header(AppConstants.HEADER_SHOP_NAME, shopName).when()
				.get(apiVersion + usersPath + username + productsPath).then().assertThat().statusCode(HttpStatus.OK)
				.extract().body().asString();
		assertEquals(jsonCatalog, catalogResponse, true);

		ProductResponse product = new ProductResponse(1L, "Item 1", shopName, "Description 1", Set.of("image1"),
				Set.of("category"), true, BigDecimal.ONE);
		String jsonProduct = mapper.writeValueAsString(product);
		productServiceMock.stubFor(get(urlMatching(apiVersion + productsPath + "/([0-9]*)")).willReturn(aResponse()
				.withStatus(HttpStatus.OK).withHeader("Content-Type", "application/json").withBody(jsonProduct)));

		String productResponse = given().header(AppConstants.HEADER_SHOP_NAME, shopName).when()
				.get(apiVersion + usersPath + username + productsPath + "/1").then().assertThat()
				.statusCode(HttpStatus.OK).extract().body().asString();
		assertEquals(jsonProduct, productResponse, true);
	}

}
