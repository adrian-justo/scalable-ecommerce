package com.apj.ecomm.cart.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.cart.item")
interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

	List<CartItem> findAllByCartId(long cartId);

}