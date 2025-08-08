package com.apj.ecomm.product.domain.model;

import java.math.BigDecimal;

public record ProductCatalog(Long id, String image, String name, BigDecimal price) {}