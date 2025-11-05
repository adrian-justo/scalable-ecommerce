package com.apj.ecomm.order.domain.model;

import org.apache.commons.lang3.StringUtils;

import com.apj.ecomm.order.constants.AppConstants;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record DeliveryInformationRequest(String name, String address,
		@Email(message = AppConstants.MSG_VALUE_INVALID) String email,
		@Pattern(regexp = AppConstants.PATTERN_MOBILE, message = AppConstants.MSG_VALUE_INVALID) String mobileNo) {

	public DeliveryInformationRequest {
		name = StringUtils.trimToNull(name);
		address = StringUtils.trimToNull(address);
		email = StringUtils.trimToNull(email);
		mobileNo = StringUtils.trimToNull(mobileNo);
	}

}