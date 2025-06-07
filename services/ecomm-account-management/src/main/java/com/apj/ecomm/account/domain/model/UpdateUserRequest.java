package com.apj.ecomm.account.domain.model;

import java.util.Set;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.web.exception.EmailSmsMissingException;
import com.apj.ecomm.account.web.exception.InvalidNotificationTypeException;
import com.apj.ecomm.account.web.exception.InvalidRoleException;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(@Email(message = AppConstants.MSG_EMAIL_INVALID) String email,
		@Pattern(regexp = AppConstants.PATTERN_MOBILE, message = AppConstants.MSG_MOBILE_INVALID) String mobileNo,
		@Pattern(regexp = AppConstants.PATTERN_PASSWORD, message = AppConstants.MSG_PASSWORD_INVALID) String password,
		String name, String shopName, String address, Set<Role> roles, Set<NotificationType> notificationTypes) {

	public void validate() {
		if (email != null && email.isBlank() && mobileNo != null && mobileNo.isBlank()) {
			throw new EmailSmsMissingException();
		}

		if (roles != null && (roles.isEmpty() || roles.stream().anyMatch(role -> !Role.isValid(role)))) {
			throw new InvalidRoleException();
		}

		if (notificationTypes != null && (notificationTypes.isEmpty()
				|| notificationTypes.stream().anyMatch(type -> !NotificationType.isValid(type)))) {
			throw new InvalidNotificationTypeException();
		}
	}

}
