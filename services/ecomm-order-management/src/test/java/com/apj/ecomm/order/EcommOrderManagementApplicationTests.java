package com.apj.ecomm.order;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import com.apj.ecomm.order.constants.AppConstants;
import com.apj.ecomm.order.domain.Status;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.web.client.cart.CartItemResponse;
import com.apj.ecomm.order.web.messaging.account.AccountInformationDetails;
import com.apj.ecomm.order.web.messaging.account.PaymentTransferRequest;
import com.apj.ecomm.order.web.messaging.account.RequestAccountInformationEvent;
import com.apj.ecomm.order.web.messaging.account.UserResponse;
import com.apj.ecomm.order.web.messaging.cart.UpdateCartItemsEvent;
import com.apj.ecomm.order.web.messaging.notification.NotificationRequest;
import com.apj.ecomm.order.web.messaging.payment.CheckoutSessionRequest;
import com.apj.ecomm.order.web.messaging.payment.UpdateOrderStatusEvent;
import com.apj.ecomm.order.web.messaging.product.OrderedProductDetails;
import com.apj.ecomm.order.web.messaging.product.ProductResponse;
import com.apj.ecomm.order.web.messaging.product.ProductStockUpdate;
import com.apj.ecomm.order.web.messaging.product.ReturnProductStockEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.restassured.RestAssured;

@ActiveProfiles("test")
@Import({ TestcontainersConfiguration.class, TestChannelBinderConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "eureka.client.enabled=false", "spring.cloud.config.enabled=false" })
class EcommOrderManagementApplicationTests {

	@Value("${api.version}")
	private String apiVersion;

	@Value("${orders.path}")
	private String path;

	@Value("${admin.path}")
	private String adminPath;

	@Value("${shop.path}")
	private String shopPath;

	@Value("${carts.path}")
	private String cartsPath;

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
	void checkoutOrder_reorder() throws IOException {
		final var buyerId = "buyerId";
		final var shopId = "shopId";
		final var product1 = new ProductResponse("Product 1", shopId, List.of("image1"), 1, BigDecimal.TWO);
		final var product2 = new ProductResponse("Product 2", shopId, List.of("image2"), 0, BigDecimal.ONE);
		final var carts = List.of(new CartItemResponse(1L, shopId, product1.stock() + 1),
				new CartItemResponse(2L, shopId, product1.stock() + 1));
		final var jsonCatalog = mapper.writeValueAsString(carts);
		clientMock.stubFor(
				get(urlEqualTo(apiVersion + cartsPath + productsPath)).willReturn(aResponse().withStatus(HttpStatus.OK)
					.withHeader("Content-Type", "application/json")
					.withBody(jsonCatalog)));

		// 1. Create checkout order request
		final var createResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType("application/json")
			.when()
			.post(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED)
			.extract()
			.body()
			.asString();
		var order = mapper.readValue(createResponse, new TypeReference<List<OrderResponse>>() {
		}).getFirst();

		// 2. Send account information request and verify
		final var accountData = output.receive(100, "account-request-user-details");
		final var accountEvent = mapper.readValue(accountData.getPayload(), RequestAccountInformationEvent.class);

		assertEquals(Status.PROCESSING.toString(), order.status());
		assertNull(order.deliveryInformation().address());
		assertNull(order.shopInformation());
		assertTrue(accountEvent.userIds().containsAll(Set.of(order.shopId())));

		// 3. Receive account information and verify
		final var accountdetails = new AccountInformationDetails(order.buyerId(), Map.of(order.buyerId(),
				new UserResponse("Buyer Name", null, "Buyer Address", "Buyer Email", null, Set.of()), order.shopId(),
				new UserResponse(null, "Shop Name", "Shop Address", null, "Shop Mobile No.", Set.of())));
		input.send(MessageBuilder.withPayload(accountdetails).build(), "order-return-user-details");

		var getResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + "/" + order.id())
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		order = mapper.readValue(getResponse, OrderResponse.class);

		assertNotNull(order.deliveryInformation().address());
		assertNotNull(order.shopInformation());

		// 4. Send product stock update and product information request for order/s and
		// verify
		final var productData = output.receive(100, "product-update-stock-ordered");
		final var productEvent = mapper.readValue(productData.getPayload(), ProductStockUpdate.class);
		assertTrue(productEvent.products().containsKey(order.products().getFirst().productId()));

		// 5. Verify that checkout request fails if buyer still has order currently in
		// processing status
		given().header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType("application/json")
			.when()
			.post(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.UNPROCESSABLE_ENTITY);

		// 6. Receive product information and verify
		final var productDetails = new OrderedProductDetails(order.buyerId(), Map.of(1L, product1, 2L, product2));
		input.send(MessageBuilder.withPayload(productDetails).build(), "order-return-product-details");

		getResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + "/" + order.id())
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		order = mapper.readValue(getResponse, OrderResponse.class);

		final var products = order.products();
		assertEquals(Status.ACTIVE.toString(), order.status());
		assertEquals(1, products.size());
		assertNotNull(products.getFirst().productDetail());

		// 7. Send checkout session request and verify
		final var checkoutData = output.receive(100, "payment-request-checkout-session");
		final var checkoutEvent = mapper.readValue(checkoutData.getPayload(), CheckoutSessionRequest.class);
		assertEquals(1, checkoutEvent.orders().size());
		assertEquals(order.id(), checkoutEvent.orders().getFirst().id());

		// 8. Send cart update for out of stock products and verify
		final var updateData = output.receive(100, "cart-update-item-quantity");
		final var updateEvent = mapper.readValue(updateData.getPayload(), UpdateCartItemsEvent.class);
		assertEquals(2, updateEvent.products().size());
		assertEquals(product2.stock(), updateEvent.products().get(carts.get(1).productId()));

		// 9. Create another checkout order request to verify product stock return event
		// and re-ordering
		given().header(AppConstants.HEADER_USER_ID, buyerId)
			.contentType("application/json")
			.when()
			.post(apiVersion + path)
			.then()
			.assertThat()
			.statusCode(HttpStatus.CREATED);
		final var returnData = output.receive(100, "product-return-stock-ordered");
		final var returnEvent = mapper.readValue(returnData.getPayload(), ReturnProductStockEvent.class);
		assertEquals(1, returnEvent.products().get(products.getFirst().productId()));

		output.receive(100, "account-request-user-details");
		input.send(MessageBuilder.withPayload(accountdetails).build(), "order-return-user-details");
		output.receive(100, "product-update-stock-ordered");
		input.send(MessageBuilder.withPayload(productDetails).build(), "order-return-product-details");
		output.receive(100, "payment-request-checkout-session");
		getResponse = given().header(AppConstants.HEADER_USER_ID, buyerId)
			.when()
			.get(apiVersion + path + "/" + order.id())
			.then()
			.assertThat()
			.statusCode(HttpStatus.OK)
			.extract()
			.body()
			.asString();
		final var order2 = mapper.readValue(getResponse, OrderResponse.class);
		assertEquals(products, order2.products());
	}

	@Test
	void message_updateStatusAndRequestTransfer() throws IOException {
		final var buyerId = "buyer001";
		final var productDetails = new UpdateOrderStatusEvent(buyerId, Status.CONFIRMED, "paymentIntentId");
		input.send(MessageBuilder.withPayload(productDetails).build(), "order-update-order-status");

		final var transferData = output.receive(100, "account-request-payment-transfer");
		final var transferEvent = mapper.readValue(transferData.getPayload(), PaymentTransferRequest.class);
		assertEquals(2, transferEvent.transferDetails().size());
		assertEquals(productDetails.paymentIntentId(), transferEvent.paymentIntentId());

		final var notifData = output.receive(100, "notification-send-event-update");
		final var notifEvent = mapper.readValue(notifData.getPayload(), NotificationRequest.class);
		assertEquals(productDetails.status(), notifEvent.status());
	}

}
