package com.apj.ecomm.order.web.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.apj.ecomm.order.config.FeignConfig;
import com.apj.ecomm.order.constants.AppConstants;
import com.apj.ecomm.order.web.client.cart.CartItemResponse;

import io.micrometer.observation.annotation.Observed;

@FeignClient(name = "ECOMM-API-GATEWAY", configuration = FeignConfig.class)
@Observed(name = "client.order")
public interface OrderClient {

	@GetMapping("${api.version}${carts.path}${products.path}")
	List<CartItemResponse> getAllCartItems(@RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId);

}
