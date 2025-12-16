package com.apj.ecomm.product.web.messaging.account;

public record ShopStatusUpdatedEvent(String shopId, Boolean active) {
}