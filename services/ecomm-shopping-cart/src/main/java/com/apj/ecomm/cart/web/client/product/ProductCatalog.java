package com.apj.ecomm.cart.web.client.product;

import java.math.BigDecimal;

public record ProductCatalog(Long id, String image, String name, BigDecimal price) {}