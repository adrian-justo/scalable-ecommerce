package com.apj.ecomm.product.domain;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
		imports = { Optional.class, HashSet.class, Collectors.class })
interface ProductMapper {

	@Mapping(target = "stock", defaultValue = "1")
	@Mapping(target = "price", defaultExpression = "java(new BigDecimal(\"" + AppConstants.PRICE_DEFAULT + "\"))")
	ProductResponse toResponse(Product product);

	@Mapping(target = "image",
			expression = "java(Optional.ofNullable(product.getImages()).orElse(new HashSet<>()).stream().findFirst().orElse(\""
					+ AppConstants.IMAGE_DEFAULT + "\"))")
	ProductCatalog toCatalog(Product product);

	@Mapping(target = "categories",
			expression = "java(request.categories() != null ? request.categories().stream().map(String::toLowerCase).collect(Collectors.toSet()) : null)")
	Product toEntity(CreateProductRequest request);

	@Mapping(target = "categories",
			expression = "java(updated.categories() != null ? updated.categories().stream().map(String::toLowerCase).collect(Collectors.toSet()) : null)")
	Product updateEntity(UpdateProductRequest updated, @MappingTarget Product existing);

}
