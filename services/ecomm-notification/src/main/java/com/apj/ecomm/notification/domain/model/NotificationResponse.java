package com.apj.ecomm.notification.domain.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(String id, String userId, Message message, boolean successful, String error,
		Instant createdAt, Instant updatedAt) {
}