package com.apj.ecomm.order.web.messaging;

import java.util.Map;

public record OrderedProductDetails(String buyerId, Map<Long, ProductResponse> details) {
}