package com.apj.ecomm.product.domain.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.web.exception.RequestArgumentNotValidException;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateProductRequest(String name, String description, List<String> images, Set<String> categories,
		@PositiveOrZero(message = AppConstants.MSG_VALUE_GT_EQ + 0) Integer stock,
		@Digits(integer = AppConstants.PRICE_PRECISION, fraction = AppConstants.PRICE_SCALE) @DecimalMin(
				value = AppConstants.PRICE_DEFAULT,
				message = AppConstants.MSG_VALUE_GT_EQ + AppConstants.PRICE_DEFAULT) BigDecimal price) {

	public void validate() {
		final var errors = new HashMap<String, List<String>>();

		if (name != null && name.isBlank()) {
			errors.put("name", List.of(AppConstants.MSG_FIELD_BLANK));
		}

		if (!errors.isEmpty())
			throw new RequestArgumentNotValidException(errors);
	}

}
