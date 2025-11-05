package com.apj.ecomm.order.domain.model;

import com.apj.ecomm.order.constants.AppConstants;

import jakarta.validation.constraints.NotEmpty;

public record CompleteOrderRequest(@NotEmpty(message = AppConstants.MSG_FIELD_BLANK) String trackingNumber,
		String courierCode) {

	public CompleteOrderRequest(final String trackingNumber) {
		this(trackingNumber, null);
	}

}