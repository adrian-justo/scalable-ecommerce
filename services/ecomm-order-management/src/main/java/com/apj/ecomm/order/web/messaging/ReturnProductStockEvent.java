package com.apj.ecomm.order.web.messaging;

import java.util.Map;

public record ReturnProductStockEvent(Map<Long, Integer> products) {
}