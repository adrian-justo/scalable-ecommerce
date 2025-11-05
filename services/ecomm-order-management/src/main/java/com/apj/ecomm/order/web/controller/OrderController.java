package com.apj.ecomm.order.web.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.order.constants.AppConstants;
import com.apj.ecomm.order.domain.IOrderService;
import com.apj.ecomm.order.domain.model.DeliveryInformationRequest;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
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

@Tag(name = "Order API", description = "Endpoints for viewing and creating orders")
@RestController
@RequestMapping("${api.version}${orders.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.order")
@CrossOrigin
@RequiredArgsConstructor
public class OrderController {

	private final IOrderService service;

	@Operation(summary = "Order History", description = "View all your orders")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Orders" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<OrderResponse> getAllOrdersOfBuyer(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt",
					direction = Sort.Direction.DESC) final Pageable pageable) {
		return service.findAllBy(buyerId, pageable);
	}

	@Operation(summary = "Order Details", description = "View details of a specific order")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Order" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Order" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED
							+ "order",
					content = @Content) })
	@GetMapping("/{orderId}")
	public OrderResponse getOrderById(@PathVariable final long orderId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		PathValidator.orderId(orderId);
		return service.findById(orderId, buyerId);
	}

	@Operation(summary = "Checkout", description = "Create a checkout order on your shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Checkout order created successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Cart item" + AppConstants.MSG_NOT_FOUND,
					content = @Content),
			@ApiResponse(responseCode = "422", description = AppConstants.MSG_UNPROCESSABLE_ENTITY,
					content = @Content) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public List<OrderResponse> checkOut(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@RequestBody(required = false) @Valid final DeliveryInformationRequest request) {
		return service.checkOut(buyerId, request);
	}

}
