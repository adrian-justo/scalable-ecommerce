package com.apj.ecomm.account.domain.model;

import com.apj.ecomm.account.constants.AppConstants;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = AppConstants.MSG_IDENTIFIER_BLANK) String identifier,
		@NotBlank(message = AppConstants.MSG_PASSWORD_BLANK) String password) {

}
