package com.apj.ecomm.product.web.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.apj.ecomm.product.domain.model.Paged;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product API", description = "Endpoints for viewing and managing products")
@RestController
@RequestMapping("${api.version}${products.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@Observed(name = "controller.product")
@CrossOrigin
@RequiredArgsConstructor
public class ProductController {

	private final IProductService service;

	private static final String DESCRIPTION_FILTER = """
			This filter is used to search products based on various criteria.
			The filter can be a simple combination of a field, an operation and the value to compare with,
			or a combination of conditions using a conjunction, and grouping.<br>
			For timestamp fields, use the format `YYYY-MM-DDTHH:mm:ssZ`.<br>
			For `price` field, using comma-separated values `#,##0.00` may not work as intended or result in parsing error,
			 since `,` is interpreted as an operation modifier, please ensure commas are removed.
			<br><br>
			|Field Name|Type|
			|--|--|
			|id|number|
			|name|text|
			|shopId|text|
			|shopName|text|
			|description|text|
			|categories|collection(text)|
			|stock|number|
			|price|number|
			|createdAt|timestamp|
			|updatedAt|timestamp|
			<br><br>
			|Symbol|Type|Description(Field Type Limitation)|Usage|
			|--|--|--|--|
			|`:`|operation|equal to/has|categories`:`perfumes|
			|`%`|operation|containing(text except collection)|name`%`airpods|
			|`<`|operation|less than or equal to(non-text)|price`<`10|
			|`>`|operation|greater than or equal to(non-text)|stock`>`1|
			|`!`|operation modifier|negation|shopName`!%`shop|
			|`->`|operation modifier|between/ranging(non-text)|createdAt:2025-01-01T00:00:00Z`->`2025-01-01T23:59:59Z|
			|`,`|operation modifier|in/any of(non-collection)|id:1`,`2`,`3|
			|`;`|conjunction|and|price<10`;`stock>1|
			|&vert;|conjunction|or|price<10 &vert; stock>1|
			|`(`...`)`|grouping|combined conditions|`(`price<10;stock>1`)`|
			""";

	private static final String EXAMPLE_COMBINATION = "categories:perfumes|(price<10;stock>1)|createdAt:2025-01-01T00:00:00Z->2025-01-01T23:59:59Z";

	private static final String DESCRIPTION_EXAMPLE_COMBINATION = """
			`categories` has 'perfumes'<br>
			or a combination where `price` is less than or equal to 10 and `stock` is greater than or equal to 1<br>
			or `createdAt` Jan. 1, 2025 between 00:00:00 and 23:59:59
			""";

	@Operation(summary = "Product Catalog", description = "View all products")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Products" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content) })
	@GetMapping
	public Paged<ProductCatalog> getAllProducts(
			@Parameter(description = DESCRIPTION_FILTER,
					examples = {
							@ExampleObject(name = "Simple", value = "name%airpods",
									description = "`name` containing 'airpods'"),
							@ExampleObject(name = "Combination", value = EXAMPLE_COMBINATION,
									description = DESCRIPTION_EXAMPLE_COMBINATION) }) @RequestParam(
											defaultValue = "stock>1") final String filter,
			@ParameterObject @PageableDefault(page = 0, size = AppConstants.DEFAULT_PAGE_SIZE,
					sort = { "price" }) final Pageable pageable) {
		return service.findAll(filter, pageable);
	}

	@Operation(summary = "Product Details", description = "View details of a specific product")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "404", description = "Product" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@GetMapping("/{productId}")
	public ProductResponse getProductById(@PathVariable final long productId) {
		validate(productId);
		return service.findById(productId);
	}

	@Operation(summary = "List Product", description = "List a new product entry")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Product listed successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirement(name = "authToken")
	public ProductResponse listProduct(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_SHOP_NAME) final String shopName,
			@RequestBody @Valid final CreateProductRequest request) {
		return service.list(shopId, shopName, request);
	}

	@Operation(summary = "Update Product", description = "Update details of a specific product")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Product updated successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Product" + AppConstants.MSG_NOT_FOUND+ " / " + AppConstants.MSG_ACCESS_DENIED + "product",
					content = @Content) })
	@PutMapping("/{productId}")
	@SecurityRequirement(name = "authToken")
	public ProductResponse updateProduct(@PathVariable final long productId,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId,
			@RequestBody @Valid final UpdateProductRequest request) {
		validate(productId);
		request.validate();
		return service.update(productId, shopId, request);
	}

	private void validate(final long productId) {
		if (productId <= 0)
			throw new ResourceNotFoundException();
	}

}
