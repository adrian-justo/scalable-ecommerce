package com.apj.ecomm.product.web.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.IProductService;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@Observed(name = "controller.product")
@RequiredArgsConstructor
public class ProductController {

	private final IProductService service;

	@GetMapping
	public List<ProductCatalog> getAllProducts(@RequestParam(defaultValue = "stock>1") String filter,
			@ParameterObject @PageableDefault(page = 0, size = 10, sort = { "price" }) Pageable pageable) {
		return service.findAll(filter, pageable);
	}

	@GetMapping("/{productId}")
	public ProductResponse getProductById(@PathVariable long productId) {
		validate(productId);
		return service.findById(productId);
	}

	@GetMapping("/{productId}/stock")
	public Integer getProductStock(@PathVariable long productId) {
		validate(productId);
		return service.getStock(productId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirement(name = "authToken")
	public ProductResponse listProduct(@RequestHeader(AppConstants.HEADER_SHOP_NAME) String shopName,
			@RequestBody @Valid CreateProductRequest request) {
		return service.list(shopName, request);
	}

	@PutMapping("/{productId}")
	@SecurityRequirement(name = "authToken")
	public ProductResponse updateProduct(@PathVariable long productId,
			@RequestBody @Valid UpdateProductRequest request) {
		validate(productId);
		request.validate();
		return service.update(productId, request);
	}

	private void validate(long productId) {
		if (productId <= 0) {
			throw new ResourceNotFoundException();
		}
	}

}
