package com.apj.ecomm.account.domain.model;

import java.util.List;

import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(@Email(message = "Email is invalid") String email,
		@Pattern(regexp = "^$|(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Mobile no. is invalid") String mobileNo,
		@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W])(?=\\S+$).{8,}$", message = "Password is not strong enough") String password,
		String name, String shopName, String address, List<Role> roles, List<NotificationType> notificationTypes) {

}
