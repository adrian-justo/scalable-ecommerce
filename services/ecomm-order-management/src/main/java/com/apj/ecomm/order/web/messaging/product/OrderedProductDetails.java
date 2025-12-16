package com.apj.ecomm.order.web.messaging.product;

import java.util.Map;

public record OrderedProductDetails(String buyerId, Map<Long, ProductResponse> details) {
}