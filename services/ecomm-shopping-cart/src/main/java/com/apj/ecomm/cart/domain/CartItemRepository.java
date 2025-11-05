package com.apj.ecomm.cart.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.cart.product")
interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

}