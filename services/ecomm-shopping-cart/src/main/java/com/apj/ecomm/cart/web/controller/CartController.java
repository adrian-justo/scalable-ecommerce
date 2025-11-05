package com.apj.ecomm.cart.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.cart.constants.AppConstants;
import com.apj.ecomm.cart.domain.ICartService;
import com.apj.ecomm.cart.domain.model.CartDetailResponse;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Shopping Cart API",
		description = "Endpoints for viewing and managing shopping cart items.<br>"
				+ "Use the ../users/{username}/carts endpoint to view your shopping cart details.")
@RestController
@RequestMapping("${api.version}${carts.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.cart.product")
@CrossOrigin
@RequiredArgsConstructor
public class CartController {

	private final ICartService service;

	@Hidden
	@GetMapping
	public CartDetailResponse getCartOfBuyer(@RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		return service.findCartBy(buyerId);
	}

	@Operation(summary = "Cart Items", description = "View all products in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Products" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping("${products.path}")
	public List<CartItemResponse> getAllCartItems(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		return service.findItemsBy(buyerId);
	}

	@Operation(summary = "Cart Item Details", description = "View details of product in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Cart / Item" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@GetMapping("${products.path}/{productId}")
	public CartItemDetail getCartItemBy(@PathVariable final long productId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		PathValidator.productId(productId);
		return service.findItemBy(productId, buyerId);
	}

	@Operation(summary = "Add To Cart", description = "Add product/s to shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Product/s listed successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@PostMapping("${products.path}")
	@ResponseStatus(HttpStatus.CREATED)
	public List<CartItemResponse> addToCart(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@RequestBody @Valid final List<CartItemRequest> requestList) {
		return service.addItems(buyerId, requestList);
	}

	@Operation(summary = "Update Quantity", description = "Update the quantity for product/s in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product/s updated successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@PutMapping("${products.path}")
	public List<CartItemResponse> updateQuantity(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@ArraySchema(schema = @Schema(
					implementation = CartItemRequest.class)) @RequestBody @Valid final List<CartItemRequest> requestList) {
		return service.updateItems(buyerId, requestList);
	}

	@Operation(summary = "Remove Items", description = "Remove product/s in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Product/s removed successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@DeleteMapping("${products.path}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeFromCart(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@RequestParam final List<Long> id) {
		service.deleteItems(buyerId, id);
	}

}
