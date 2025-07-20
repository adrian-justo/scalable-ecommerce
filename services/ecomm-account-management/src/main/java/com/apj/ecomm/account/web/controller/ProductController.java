package com.apj.ecomm.account.web.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.ProductClient;
import com.apj.ecomm.account.domain.model.ProductCatalog;
import com.apj.ecomm.account.domain.model.ProductResponse;
import com.apj.ecomm.account.web.controller.util.PathValidator;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users/{username}/products")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.user.product")
@RequiredArgsConstructor
public class ProductController {

	private final ProductClient client;

	@GetMapping
	public List<ProductCatalog> getAllProductsOfSeller(@PathVariable String username,
			@RequestHeader(AppConstants.HEADER_SHOP_NAME) String shopName,
			@ParameterObject @PageableDefault(page = 0, size = 10, sort = { "stock" }) Pageable pageable) {
		PathValidator.username(username);
		return client.getAllProducts("stock>0;shopName:" + shopName, pageable);
	}

	@GetMapping("/{productId}")
	public ProductResponse getProductOfSellerById(@PathVariable String username,
			@RequestHeader(AppConstants.HEADER_SHOP_NAME) String shopName, @PathVariable long productId) {
		PathValidator.username(username);
		PathValidator.productId(productId);

		ProductResponse response = client.getProductById(productId);
		if (!response.shopName().equals(shopName)) {
			throw new ResourceNotFoundException("Product");
		}
		return response;
	}

}
