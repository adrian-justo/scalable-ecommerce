package com.apj.ecomm.account.web.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.apj.ecomm.account.config.FeignConfig;
import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.web.client.cart.BuyerCartResponse;
import com.apj.ecomm.account.web.client.product.ProductCatalog;
import com.apj.ecomm.account.web.client.product.ProductResponse;

import io.micrometer.observation.annotation.Observed;

@FeignClient(name = "ECOMM-API-GATEWAY", configuration = FeignConfig.class)
@Observed(name = "client.user")
public interface AccountClient {

	@GetMapping("${api.version}${products.path}")
	Paged<ProductCatalog> getAllProducts(@RequestParam String filter, Pageable pageable);

	@GetMapping("${api.version}${products.path}/{productId}")
	ProductResponse getProductById(@PathVariable long productId);

	@GetMapping("${api.version}${carts.path}/buyer")
	BuyerCartResponse getCartOfBuyer(@RequestHeader(AppConstants.HEADER_USER_ID) String buyerId);

}
