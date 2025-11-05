package com.apj.ecomm.order.domain.model;

import java.math.BigDecimal;

public record OrderItemDetailResponse(String image, String name, BigDecimal price) {
}