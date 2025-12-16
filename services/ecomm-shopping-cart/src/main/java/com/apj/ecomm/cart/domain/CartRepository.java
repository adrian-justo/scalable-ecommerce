package com.apj.ecomm.cart.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.cart")
interface CartRepository extends JpaRepository<Cart, Long> {

	Optional<Cart> findByBuyerIdAndActiveTrue(String buyerId);

}