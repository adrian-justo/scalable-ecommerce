package com.apj.ecomm.product.web.messaging;

import java.util.Map;

import com.apj.ecomm.product.domain.model.ProductResponse;

public record OrderedProductDetails(String buyerId, Map<Long, ProductResponse> details) {
}