package com.apj.ecomm.account.domain;

import java.util.Arrays;

import io.swagger.v3.oas.annotations.Hidden;

public enum Role {

	BUYER, SELLER, @Hidden
	ADMIN;

	public static boolean isValid(Role role) {
		if (role.equals(ADMIN)) {
			return false;
		}
		return Arrays.stream(Role.values()).anyMatch(role::equals);
	}
}
