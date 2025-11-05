package com.apj.ecomm.order.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(Long id, String buyerId, InformationResponse deliveryInformation, String shopId,
		InformationResponse shopInformation, List<OrderItemResponse> products, BigDecimal total, Integer totalProducts,
		Integer totalQuantity, String trackingNumber, String status, Instant createdAt, Instant updatedAt) {
}