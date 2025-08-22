package com.apj.ecomm.cart.constants;

public final class AppConstants {

	private AppConstants() {
		// Prevent instantiation
	}

	public static final String MSG_FIELD_NULL = "This field is required";

	public static final String MSG_NUMBER_INVALID = "Value must be greater than 0";

	public static final String MSG_OK = " found and returned";

	public static final String MSG_NOT_FOUND = " is not found";

	public static final String MSG_BAD_REQUEST = "Details provided is invalid";

	public static final String MSG_ACCESS_DENIED = "User does not have access to this ";

	public static final String MSG_FORBIDDEN = "User session expired / " + MSG_ACCESS_DENIED + "endpoint";

	public static final String HEADER_USER_ID = "ecomm-user-id";

}
