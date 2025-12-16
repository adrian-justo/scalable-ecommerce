package com.apj.ecomm.payment.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.payment")
interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByBuyerIdAndStatus(String buyerId, String status);

}
