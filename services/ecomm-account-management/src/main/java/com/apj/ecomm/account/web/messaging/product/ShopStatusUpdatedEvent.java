package com.apj.ecomm.account.web.messaging.product;

public record ShopStatusUpdatedEvent(String shopId, Boolean active) {
}