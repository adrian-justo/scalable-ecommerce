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
import com.apj.ecomm.cart.domain.ICartItemService;
import com.apj.ecomm.cart.domain.model.CartItemCatalog;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;
import com.apj.ecomm.cart.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
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

@Tag(name = "Shopping Cart Item API", description = "Endpoints for viewing of product details and cart item management")
@RestController
@RequestMapping("${api.version}${carts.path}/{cartId}${products.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.cart.product")
@CrossOrigin
@RequiredArgsConstructor
public class CartItemController {

	private final ICartItemService service;

	@Operation(summary = "Cart Items", description = "View all products in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Products" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Cart" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED + "cart",
					content = @Content) })
	@GetMapping
	public List<CartItemCatalog> getAllCartItems(@PathVariable final long cartId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		PathValidator.cartId(cartId);
		return service.findAll(cartId, buyerId);
	}

	@Operation(summary = "Cart Item Details", description = "View details of product in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Cart/Product" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED
							+ "resource",
					content = @Content) })
	@GetMapping("/{productId}")
	public CartItemDetail getCartItemById(@PathVariable final long cartId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@PathVariable final long productId) {
		PathValidator.cartId(cartId);
		PathValidator.productId(productId);
		return service.findById(cartId, buyerId, productId);
	}

	@Operation(summary = "Add To Cart", description = "Add product/s to shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Product/s listed successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Cart" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED + "cart",
					content = @Content) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public List<CartItemResponse> addToCart(@PathVariable final long cartId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@RequestBody @Valid final List<CartItemRequest> requestList) {
		PathValidator.cartId(cartId);
		return service.addAll(cartId, buyerId, requestList);
	}

	@Operation(summary = "Update Quantity", description = "Update the quantity for product/s in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product/s updated successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Cart" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED + "cart",
					content = @Content) })
	@PutMapping
	public List<CartItemResponse> updateQuantity(@PathVariable final long cartId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@ArraySchema(schema = @Schema(
					implementation = CartItemRequest.class)) @RequestBody @Valid final List<CartItemRequest> requestList) {
		PathValidator.cartId(cartId);
		return service.updateAll(cartId, buyerId, requestList);
	}

	@Operation(summary = "Remove Items", description = "Remove product/s in shopping cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Product/s removed successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Cart" + AppConstants.MSG_NOT_FOUND + " / " + AppConstants.MSG_ACCESS_DENIED + "cart",
					content = @Content) })
	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeFromCart(@PathVariable final long cartId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId,
			@RequestParam final List<Long> id) {
		PathValidator.cartId(cartId);
		service.deleteAll(cartId, buyerId, id);
	}

}
