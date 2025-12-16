package com.apj.ecomm.account.web.util;

import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.ResourceAccessDeniedException;

public class AccessValidator {

	private AccessValidator() {
		// Prevent instantiation
	}

	public static void hasRole(final Role role, final UserResponse user) {
		if (!user.roles().contains(role))
			throw new ResourceAccessDeniedException("endpoint");
	}

}
