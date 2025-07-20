package com.apj.ecomm.account.domain;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.apj.ecomm.account.config.AppConfig;
import com.apj.ecomm.account.domain.model.ProductCatalog;
import com.apj.ecomm.account.domain.model.ProductResponse;

import io.micrometer.observation.annotation.Observed;

@FeignClient(name = "ECOMM-API-GATEWAY", configuration = AppConfig.class)
@Observed(name = "service.user.product")
public interface ProductClient {

	@GetMapping("/api/v1/products")
	public List<ProductCatalog> getAllProducts(@RequestParam String filter, Pageable pageable);

	@GetMapping("/api/v1/products/{productId}")
	public ProductResponse getProductById(@PathVariable long productId);

}
