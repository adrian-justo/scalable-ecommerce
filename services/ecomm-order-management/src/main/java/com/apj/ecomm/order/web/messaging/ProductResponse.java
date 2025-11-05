package com.apj.ecomm.order.web.messaging;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(String name, String shopId, List<String> images, Integer stock, BigDecimal price) {
}