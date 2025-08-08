package com.apj.ecomm.account.domain.model;

import com.apj.ecomm.account.constants.AppConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@Schema(description = AppConstants.MSG_IDENTIFIER_BLANK) @NotBlank(
				message = AppConstants.MSG_IDENTIFIER_BLANK) String identifier,
		@NotBlank(message = AppConstants.MSG_FIELD_BLANK) String password) {}