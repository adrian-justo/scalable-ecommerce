package com.apj.ecomm.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

}