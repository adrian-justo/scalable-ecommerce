package com.apj.ecomm.product.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.product")
interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	List<Product> findAllByShopId(String shopId);

	List<Product> findAllByShopIdAndActive(String shopId, boolean active);

}