package com.apj.ecomm.cart.web.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.cart.constants.AppConstants;
import com.apj.ecomm.cart.domain.ICartService;
import com.apj.ecomm.cart.domain.model.BuyerCartResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Shopping Carts API",
		description = "Endpoints for viewing shopping carts. Only an administrator can access these endpoints.<br>"
				+ "Use the ../users/{username}/carts endpoint to view your cart details.")
@RestController
@RequestMapping("${api.version}${carts.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.cart")
@CrossOrigin
@RequiredArgsConstructor
public class CartController {

	private final ICartService service;

	@Operation(summary = "Carts Audit", description = "View all shopping carts")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Carts" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<CartResponse> getAllCarts(
			@ParameterObject @PageableDefault(page = 0, size = 10) final Pageable pageable) {
		return service.findAll(pageable);
	}

	@Operation(summary = "Cart Details Audit", description = "View details of a specific cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cart" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Cart" + AppConstants.MSG_NOT_FOUND, content = @Content) })
	@GetMapping("/{cartId}")
	public CartResponse getCartById(@PathVariable final long cartId) {
		PathValidator.cartId(cartId);
		return service.findById(cartId);
	}

	@Hidden
	@GetMapping("/buyer")
	public BuyerCartResponse getCartOfBuyer(@RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		return service.findByBuyerId(buyerId);
	}

}
