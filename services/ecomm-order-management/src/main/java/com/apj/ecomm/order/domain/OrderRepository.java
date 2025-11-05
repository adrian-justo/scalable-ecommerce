package com.apj.ecomm.order.domain;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.order")
interface OrderRepository extends JpaRepository<Order, Long> {

	Page<Order> findAllByBuyerIdAndStatusNot(String buyerId, String status, Pageable pageable);

	boolean existsByBuyerIdAndStatus(String buyerId, String status);

	List<Order> findAllByBuyerIdAndStatusIn(String buyerId, List<String> statusList);

	List<Order> findAllByBuyerIdAndStatus(String buyerId, String status);

	Page<Order> findAllByShopIdAndStatusNot(String shopId, String status, Pageable pageable);

	boolean existsByShopIdAndStatusIn(String shopId, List<String> statusList);

}
