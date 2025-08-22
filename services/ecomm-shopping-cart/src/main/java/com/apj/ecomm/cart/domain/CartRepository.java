package com.apj.ecomm.cart.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.cart")
interface CartRepository extends JpaRepository<Cart, Long> {

	List<Cart> findAllByBuyerId(String buyerId);

}