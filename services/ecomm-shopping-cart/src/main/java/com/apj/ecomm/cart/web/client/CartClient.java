package com.apj.ecomm.cart.web.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apj.ecomm.cart.config.FeignConfig;
import com.apj.ecomm.cart.domain.model.Paged;
import com.apj.ecomm.cart.web.client.product.ProductResponse;

import io.micrometer.observation.annotation.Observed;

@FeignClient(name = "ECOMM-API-GATEWAY", configuration = FeignConfig.class)
@Observed(name = "client.cart")
public interface CartClient {

	@GetMapping("${api.version}${products.path}")
	Paged<ProductResponse> getAllProducts(@RequestParam String filter, Pageable pageable);

}
