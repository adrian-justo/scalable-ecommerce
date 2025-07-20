package com.apj.ecomm.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.product")
interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}