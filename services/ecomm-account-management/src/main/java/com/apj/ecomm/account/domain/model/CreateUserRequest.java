package com.apj.ecomm.account.domain.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.web.exception.RequestArgumentNotValidException;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(@NotBlank(message = AppConstants.MSG_FIELD_BLANK) String username,
		@Email(message = AppConstants.MSG_VALUE_INVALID) String email,
		@Pattern(regexp = AppConstants.PATTERN_MOBILE, message = AppConstants.MSG_VALUE_INVALID) String mobileNo,
		@NotBlank(message = AppConstants.MSG_FIELD_BLANK) @Pattern(regexp = AppConstants.PATTERN_PASSWORD, message = AppConstants.MSG_PASSWORD_INVALID) String password,
		String name, Set<Role> roles, Set<NotificationType> notificationTypes) {

	public void validate() {
		Map<String, List<String>> errors = new HashMap<>();

		if (email != null && email.isBlank() && mobileNo != null && mobileNo.isBlank()) {
			errors.put("email, mobile", List.of(AppConstants.MSG_EMAIL_MOBILE_BLANK));
		}
		if (roles != null && (roles.isEmpty() || roles.stream().anyMatch(role -> !Role.isValid(role)))) {
			errors.put("roles", List.of(AppConstants.MSG_SET_INVALID));
		}
		if (notificationTypes != null && (notificationTypes.isEmpty()
				|| notificationTypes.stream().anyMatch(type -> !NotificationType.isValid(type)))) {
			errors.put("notificationTypes", List.of(AppConstants.MSG_SET_INVALID));
		}

		if (!errors.isEmpty()) {
			throw new RequestArgumentNotValidException(errors);
		}
	}

}
