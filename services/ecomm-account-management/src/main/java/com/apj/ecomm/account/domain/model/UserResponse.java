package com.apj.ecomm.account.domain.model;

import java.util.Set;

import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;

public record UserResponse(String username, String email, String mobileNo, String password, String name,
		String shopName, String address, Set<Role> roles, Set<NotificationType> notificationTypes, boolean active) {

}
