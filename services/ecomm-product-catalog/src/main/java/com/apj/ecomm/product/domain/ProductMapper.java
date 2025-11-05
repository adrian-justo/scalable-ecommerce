package com.apj.ecomm.product.domain;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductFromMessageRequest;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface ProductMapper {

	@Mapping(target = "stock", defaultValue = "1")
	@Mapping(target = "price", defaultExpression = "java(new BigDecimal(\"" + AppConstants.PRICE_DEFAULT + "\"))")
	@Mapping(target = "images", defaultExpression = "java(List.of(\"" + AppConstants.IMAGE_DEFAULT + "\"))")
	ProductResponse toResponse(Product product);

	Product toEntity(CreateProductRequest request);

	Product updateEntity(UpdateProductRequest updated, @MappingTarget Product existing);

	@BeanMapping(qualifiedByName = "updateStock")
	Product updateEntity(UpdateProductFromMessageRequest updated, @MappingTarget Product existing);

	@Named("updateStock")
	@AfterMapping
	default void updateStock(final UpdateProductFromMessageRequest updated, @MappingTarget final Product existing) {
		final var quantity = updated.quantity();
		if (quantity != null) {
			if (updated.isOrder()) {
				existing.order(quantity);
			}
			else {
				existing.restock(quantity);
			}
		}
	}

	default Set<String> from(final Set<String> categories) {
		return categories == null || categories.isEmpty() ? categories
				: categories.stream().map(StringUtils::lowerCase).collect(Collectors.toSet());
	}

}
