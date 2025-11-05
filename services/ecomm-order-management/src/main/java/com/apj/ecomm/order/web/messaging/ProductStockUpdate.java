package com.apj.ecomm.order.web.messaging;

import java.util.Map;

public record ProductStockUpdate(String buyerId, Map<Long, Integer> products) {
}