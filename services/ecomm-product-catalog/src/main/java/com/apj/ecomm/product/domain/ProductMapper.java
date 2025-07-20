package com.apj.ecomm.product.domain;

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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = {
		java.util.Objects.class, java.util.Optional.class })
interface ProductMapper {

	@Mapping(target = "inStock", expression = "java(Optional.ofNullable(product.getStock()).orElse(1) > 0)")
	@Mapping(target = "price", expression = "java(Optional.ofNullable(product.getPrice()).orElse(new BigDecimal(\""
			+ AppConstants.PRICE_DEFAULT + "\")))")
	ProductResponse toResponse(Product product);

	@Mapping(target = "image", expression = "java(product.getImages().stream().findFirst().orElse(null))")
	@Mapping(target = "price", expression = "java(Objects.toString(product.getPrice()))")
	ProductCatalog toCatalog(Product product);

	Product toEntity(CreateProductRequest request);

	Product updateEntity(UpdateProductRequest updated, @MappingTarget Product existing);

}
