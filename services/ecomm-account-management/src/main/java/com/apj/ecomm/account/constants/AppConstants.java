package com.apj.ecomm.account.constants;

public final class AppConstants {

	private AppConstants() {
		// Prevent instantiation
	}

	public static final String PATTERN_MOBILE = "^$|(\\+\\d{1,3}[- ]?)?\\d{10}$";
	public static final String PATTERN_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W])(?=\\S+$).{8,}$";
	public static final String MSG_VALUE_INVALID = "Value provided did not match a valid format";
	public static final String MSG_PASSWORD_INVALID = "Your password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character";
	public static final String MSG_FIELD_BLANK = "This field is required";
	public static final String MSG_IDENTIFIER_BLANK = "Please input your registered username, email, or mobile number";
	public static final String MSG_EMAIL_MOBILE_BLANK = "Please provide at least one of email or mobile number";
	public static final String MSG_SET_INVALID = "Set provided must not be empty and only contain valid values";
	public static final String HEADER_SHOP_NAME = "ecomm-shop-name";
}
