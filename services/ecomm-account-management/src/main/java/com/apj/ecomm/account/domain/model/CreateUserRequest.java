package com.apj.ecomm.account.domain.model;

import java.util.Set;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(@NotBlank(message = AppConstants.MSG_FIELD_BLANK) String username,
		@Email(message = AppConstants.MSG_VALUE_INVALID) String email,
		@Pattern(regexp = AppConstants.PATTERN_MOBILE, message = AppConstants.MSG_VALUE_INVALID) String mobileNo,
		@Schema(description = AppConstants.MSG_PASSWORD_INVALID, minLength = 8) @NotBlank(
				message = AppConstants.MSG_FIELD_BLANK) @Pattern(regexp = AppConstants.PATTERN_PASSWORD,
						message = AppConstants.MSG_PASSWORD_INVALID) String password,
		String name, String shopName, String address, Set<Role> roles, Set<NotificationType> notificationTypes) {
}