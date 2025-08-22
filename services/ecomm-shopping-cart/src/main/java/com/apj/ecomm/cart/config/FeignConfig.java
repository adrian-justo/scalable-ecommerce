package com.apj.ecomm.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.cart.web.client.TokenInterceptor;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

	@Bean
	RequestInterceptor tokenInterceptor() {
		return new TokenInterceptor();
	}

}
