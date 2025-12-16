package com.apj.ecomm.product.web.messaging.order;

import java.util.Map;

public record ProductStockUpdate(String buyerId, Map<Long, Integer> products) {
}