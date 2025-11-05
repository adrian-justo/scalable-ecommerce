package com.apj.ecomm.cart.domain.model;

import com.apj.ecomm.cart.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(
		@NotNull(message = AppConstants.MSG_FIELD_NULL) @Positive(
				message = AppConstants.MSG_NUMBER_INVALID) Long productId,
		@Schema(description = "Adjusts to product's current stock if exceeded<br>"
				+ "Out of stock products are automatically removed",
				defaultValue = "1") @Positive(message = AppConstants.MSG_NUMBER_INVALID) Integer quantity) {

	public CartItemRequest(final Long productId) {
		this(productId, 1);
	}

}
