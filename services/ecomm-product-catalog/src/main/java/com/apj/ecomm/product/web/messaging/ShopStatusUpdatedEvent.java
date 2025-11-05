package com.apj.ecomm.product.web.messaging;

public record ShopStatusUpdatedEvent(String shopId, Boolean active) {
}