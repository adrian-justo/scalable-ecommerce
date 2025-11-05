package com.apj.ecomm.product.web.messaging;

import java.util.Map;

public record ReturnProductStockEvent(Map<Long, Integer> products) {
}