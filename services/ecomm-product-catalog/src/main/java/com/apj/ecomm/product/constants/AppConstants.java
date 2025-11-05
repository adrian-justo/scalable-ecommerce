package com.apj.ecomm.product.constants;

public final class AppConstants {

	private AppConstants() {
		// Prevent instantiation
	}

	public static final String MSG_FIELD_BLANK = "This field is required";

	public static final String MSG_STOCK_INVALID = "Value must be greater than 0";

	public static final String MSG_OK = " found and returned";

	public static final String MSG_NOT_FOUND = " is not found";

	public static final String MSG_BAD_REQUEST = "Details provided is invalid";

	public static final String MSG_ACCESS_DENIED = "User does not have access to this ";

	public static final String MSG_FORBIDDEN = "User session expired / " + MSG_ACCESS_DENIED + "endpoint";

	public static final String MSG_FILTER_INVALID = "Error parsing filter: Invalid ";

	public static final int PRICE_PRECISION = 9;

	public static final int PRICE_SCALE = 2;

	public static final String PRICE_DEFAULT = "0.01";

	public static final String IMAGE_DEFAULT = "https://placehold.co/300";

	public static final String MSG_VALUE_GT_EQ = "Value must be greater than or equal to ";

	public static final String HEADER_USER_ID = "ecomm-user-id";

	public static final String HEADER_SHOP_NAME = "ecomm-shop-name";

}
