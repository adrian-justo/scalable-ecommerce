package com.apj.ecomm.account.domain.model;

import java.time.Instant;
import java.util.Set;

import com.apj.ecomm.account.domain.NotificationType;
import com.apj.ecomm.account.domain.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(String id, String username, String email, String mobileNo, String name, String shopName,
		String address, Set<Role> roles, Set<NotificationType> notificationTypes, Instant createdAt, Instant updatedAt,
		Boolean active) {}