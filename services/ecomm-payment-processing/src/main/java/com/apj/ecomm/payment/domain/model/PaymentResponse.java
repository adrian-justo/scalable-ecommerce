package com.apj.ecomm.payment.domain.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(Long id, String buyerId, String sessionId, String currency, Long amount,
		String sessionUrl, String transferGroup, String status, Instant createdAt, Instant updatedAt) {
}