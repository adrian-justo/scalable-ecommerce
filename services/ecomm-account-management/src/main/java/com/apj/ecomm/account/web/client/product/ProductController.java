package com.apj.ecomm.account.web.client.product;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;
import com.apj.ecomm.account.web.util.PathValidator;

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
import lombok.RequiredArgsConstructor;

@Tag(name = "My Products", description = "Endpoints for viewing your products. Only a seller can access this endpoint")
@RestController
@RequestMapping("/api/v1/users/{username}/products")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.user.product")
@CrossOrigin
@RequiredArgsConstructor
public class ProductController {

	private final ProductClient client;

	@Operation(summary = "Product Catalog", description = "View all my products")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<ProductCatalog> getAllProductsOfSeller(@PathVariable final String username,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_SHOP_ID) final String shopId,
			@ParameterObject @PageableDefault(page = 0, size = 10, sort = { "stock" }) final Pageable pageable) {
		PathValidator.username(username);
		return client.getAllProducts("stock>0;shopId:" + shopId, pageable);
	}

	@Operation(summary = "Product Details", description = "View details of my product")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Product" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@GetMapping("/{productId}")
	public ProductResponse getProductOfSellerById(@PathVariable final String username,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_SHOP_ID) final String shopId,
			@PathVariable final long productId) {
		PathValidator.username(username);
		PathValidator.productId(productId);

		final var response = client.getProductById(productId);
		if (!response.shopId().equals(shopId))
			throw new ResourceNotFoundException("Product");
		return response;
	}

}
