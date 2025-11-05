package com.apj.ecomm.order.web.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.order.constants.AppConstants;
import com.apj.ecomm.order.domain.IOrdersAuditService;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Orders Audit API", description = "Endpoints for viewing all orders")
@RestController
@RequestMapping("${api.version}${admin.path}${orders.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.admin.order")
@CrossOrigin
@RequiredArgsConstructor
public class OrdersAuditController {

	private final IOrdersAuditService service;

	@Operation(summary = "Orders Audit", description = "View all orders")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Orders" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<OrderResponse> getAllOrders(
			@ParameterObject @PageableDefault(page = 0, size = 10) final Pageable pageable) {
		return service.findAll(pageable);
	}

	@Operation(summary = "Order Audit Details", description = "View details of a specific order")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Order" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Order" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@GetMapping("/{orderId}")
	public OrderResponse getOrderById(@PathVariable final long orderId) {
		PathValidator.orderId(orderId);
		return service.findById(orderId);
	}

}
