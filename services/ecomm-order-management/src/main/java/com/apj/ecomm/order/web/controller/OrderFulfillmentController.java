package com.apj.ecomm.order.web.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.order.constants.AppConstants;
import com.apj.ecomm.order.domain.IOrderFulfillmentService;
import com.apj.ecomm.order.domain.model.CompleteOrderRequest;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Order Fulfillment API", description = "Endpoints for viewing and managing shop orders")
@RestController
@RequestMapping("${api.version}${shop.path}${orders.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.shop.order")
@CrossOrigin
@RequiredArgsConstructor
public class OrderFulfillmentController {

	private final IOrderFulfillmentService service;

	@Hidden
	@GetMapping("/exists")
	public boolean activeOrderExists(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId) {
		return service.activeOrderExists(shopId);
	}

	@Operation(summary = "Shop Orders", description = "View all shop orders")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Orders" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<OrderResponse> getAllOrdersOfSeller(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId,
			@ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt",
					direction = Sort.Direction.DESC) final Pageable pageable) {
		return service.findAllBy(shopId, pageable);
	}

	@Operation(summary = "Shop Order Details", description = "View details of a specific order")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Order" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Order" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED
							+ "order",
					content = @Content) })
	@GetMapping("/{orderId}")
	public OrderResponse getShopOrderById(@PathVariable final long orderId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId) {
		PathValidator.orderId(orderId);
		return service.findById(orderId, shopId);
	}

	@Operation(summary = "Complete Order", description = "Complete order by providing necessary shipment information")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Order completed successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Order" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED
							+ "order",
					content = @Content) })
	@PutMapping("/{orderId}")
	public OrderResponse completeOrderById(@PathVariable final long orderId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId,
			@RequestBody @Valid final CompleteOrderRequest request) {
		PathValidator.orderId(orderId);
		return service.update(orderId, shopId, request);
	}

}
