package com.apj.ecomm.product.domain.model;

import java.math.BigDecimal;
import java.util.Set;

import com.apj.ecomm.product.constants.AppConstants;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateProductRequest(@NotBlank(message = AppConstants.MSG_FIELD_BLANK) String name, String description,
		Set<String> images, Set<String> categories, @Positive(message = AppConstants.MSG_STOCK_INVALID) Integer stock,
		@Digits(integer = AppConstants.PRICE_PRECISION, fraction = AppConstants.PRICE_SCALE) @DecimalMin(value = AppConstants.PRICE_DEFAULT, message = AppConstants.MSG_VALUE_GT_EQ
				+ AppConstants.PRICE_DEFAULT) BigDecimal price) {

}
