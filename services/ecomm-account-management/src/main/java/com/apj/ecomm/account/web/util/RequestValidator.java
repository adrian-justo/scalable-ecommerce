package com.apj.ecomm.account.web.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.web.exception.RequestArgumentNotValidException;

public final class RequestValidator {

	private static final Map<String, List<String>> ERRORS = new HashMap<>();

	private RequestValidator() {
		// Prevent instantiation
	}

	public static void validate(final CreateUserRequest request) {
		ERRORS.clear();
		validate(request.email(), request.mobileNo());
		validate(request.roles(), request.shopName(), request.address());
		validate(request.notificationTypes());
		if (!ERRORS.isEmpty())
			throw new RequestArgumentNotValidException(ERRORS);
	}

	public static void validate(final UpdateUserRequest request) {
		ERRORS.clear();
		validate(request.email(), request.mobileNo());
		validate(request.roles(), request.shopName(), request.address());
		validate(request.notificationTypes());
		if (!ERRORS.isEmpty())
			throw new RequestArgumentNotValidException(ERRORS);
	}

	private static void validate(final String email, final String mobileNo) {
		if (email != null && email.isBlank() && mobileNo != null && mobileNo.isBlank()) {
			ERRORS.put("email, mobile", List.of(AppConstants.MSG_EMAIL_MOBILE_BLANK));
		}
	}

	private static void validate(final Set<Role> roles, final String shopName, final String address) {
		if (roles != null) {
			if (roles.isEmpty() || roles.stream().anyMatch(role -> !Role.isValid(role))) {
				ERRORS.put("roles", List.of(AppConstants.MSG_SET_INVALID));
			}
			if (roles.contains(Role.SELLER)) {
				if (StringUtils.isBlank(shopName)) {
					ERRORS.put("shopName", List.of(AppConstants.MSG_FIELD_BLANK));
				}
				if (StringUtils.isBlank(address)) {
					ERRORS.put("address", List.of(AppConstants.MSG_FIELD_BLANK));
				}
			}
		}
	}

	private static void validate(final Set<NotificationType> notificationTypes) {
		if (notificationTypes != null && (notificationTypes.isEmpty()
				|| notificationTypes.stream().anyMatch(type -> !NotificationType.isValid(type)))) {
			ERRORS.put("notificationTypes", List.of(AppConstants.MSG_SET_INVALID));
		}
	}

}
