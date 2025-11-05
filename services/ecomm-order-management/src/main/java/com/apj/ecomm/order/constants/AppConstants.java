package com.apj.ecomm.order.constants;

public final class AppConstants {

	private AppConstants() {
		// Prevent instantiation
	}

	public static final String PATTERN_MOBILE = "^$|(\\+\\d{1,3}[- ]?)?\\d{10}$";

	public static final String MSG_FIELD_BLANK = "This field is required";

	public static final String MSG_VALUE_INVALID = "Value provided did not match a valid format";

	public static final String MSG_OK = " found and returned";

	public static final String MSG_NOT_FOUND = " is not found";

	public static final String MSG_BAD_REQUEST = "Details provided is invalid";

	public static final String MSG_ACCESS_DENIED = "User does not have access to this ";

	public static final String MSG_FORBIDDEN = "User session expired / " + MSG_ACCESS_DENIED + "endpoint";

	public static final String MSG_UNPROCESSABLE_ENTITY = "Existing order is still being processed. Please try again later.";

	public static final String HEADER_USER_ID = "ecomm-user-id";

}
