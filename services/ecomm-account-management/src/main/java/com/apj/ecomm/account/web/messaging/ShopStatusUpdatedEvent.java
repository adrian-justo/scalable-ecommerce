package com.apj.ecomm.account.web.messaging;

public record ShopStatusUpdatedEvent(String shopId, Boolean active) {
}