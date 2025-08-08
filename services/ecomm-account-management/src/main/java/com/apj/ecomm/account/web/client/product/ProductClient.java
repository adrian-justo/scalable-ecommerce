package com.apj.ecomm.account.web.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.apj.ecomm.account.config.FeignConfig;
import com.apj.ecomm.account.domain.model.Paged;

import io.micrometer.observation.annotation.Observed;

@FeignClient(name = "ECOMM-API-GATEWAY", configuration = FeignConfig.class)
@Observed(name = "service.user.product")
interface ProductClient {

	@GetMapping("/api/v1/products")
	Paged<ProductCatalog> getAllProducts(@RequestParam String filter, Pageable pageable);

	@GetMapping("/api/v1/products/{productId}")
	ProductResponse getProductById(@PathVariable long productId);

}
