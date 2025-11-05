package com.apj.ecomm.product.web.messaging;

import java.util.Map;

public record ProductStockUpdate(String buyerId, Map<Long, Integer> products) {
}