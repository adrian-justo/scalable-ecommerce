package com.apj.ecomm.account.web.client.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ProductResponse(Long id, String name, String shopId, String shopName, String description,
		List<String> images, Set<String> categories, Integer stock, BigDecimal price) {}