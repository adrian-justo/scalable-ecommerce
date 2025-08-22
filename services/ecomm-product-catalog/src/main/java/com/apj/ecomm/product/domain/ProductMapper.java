package com.apj.ecomm.product.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface ProductMapper {

	@Mapping(target = "stock", defaultValue = "1")
	@Mapping(target = "price", defaultExpression = "java(new BigDecimal(\"" + AppConstants.PRICE_DEFAULT + "\"))")
	ProductResponse toResponse(Product product);

	@Mapping(target = "image", source = "product.images", qualifiedByName = "getFirstImage")
	ProductCatalog toCatalog(Product product);

	@Named("getFirstImage")
	default String from(final List<String> images) {
		return images == null || images.isEmpty() ? AppConstants.IMAGE_DEFAULT : images.getFirst();
	}

	Product toEntity(CreateProductRequest request);

	Product updateEntity(UpdateProductRequest updated, @MappingTarget Product existing);

	default Set<String> from(final Set<String> categories) {
		return categories == null || categories.isEmpty() ? categories
				: categories.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());
	}

}
