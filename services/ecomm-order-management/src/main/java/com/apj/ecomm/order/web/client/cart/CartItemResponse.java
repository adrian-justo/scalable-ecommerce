package com.apj.ecomm.order.web.client.cart;

public record CartItemResponse(Long productId, String shopId, Integer quantity) {
}