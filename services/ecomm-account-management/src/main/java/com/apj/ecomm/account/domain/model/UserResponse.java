package com.apj.ecomm.account.domain.model;

import java.util.List;

import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;

public record UserResponse(String username, String email, String mobileNo, String password, String name,
		String shopName, String address, List<Role> roles, List<NotificationType> notificationTypes, boolean active) {

}
