package com.apj.ecomm.account.constants;

public final class AppConstants {

	private AppConstants() {
		// Prevent instantiation
	}

	public static final String MSG_EMAIL_INVALID = "Please provide a valid email address";
	public static final String PATTERN_MOBILE = "^$|(\\+\\d{1,3}[- ]?)?\\d{10}$";
	public static final String MSG_MOBILE_INVALID = "Please provide a valid mobile number";
	public static final String PATTERN_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W])(?=\\S+$).{8,}$";
	public static final String MSG_PASSWORD_INVALID = "Password is not strong enough";
	public static final String MSG_USERNAME_BLANK = "Please input your username";
	public static final String MSG_PASSWORD_BLANK = "Please input your password";
	public static final String MSG_IDENTIFIER_BLANK = "Please input your registered username, email, or mobile number";

}
