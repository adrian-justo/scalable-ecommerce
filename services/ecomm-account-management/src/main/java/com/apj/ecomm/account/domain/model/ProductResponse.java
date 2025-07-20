package com.apj.ecomm.account.domain.model;

import java.math.BigDecimal;
import java.util.Set;

public record ProductResponse(Long id, String name, String shopName, String description, Set<String> images,
		Set<String> categories, Boolean inStock, BigDecimal price) {

}
