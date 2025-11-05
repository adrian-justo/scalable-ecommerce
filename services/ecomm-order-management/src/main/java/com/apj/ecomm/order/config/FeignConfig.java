package com.apj.ecomm.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.order.web.client.TokenInterceptor;

import feign.RequestInterceptor;

@Configuration
public class FeignConfig {

	@Bean
	RequestInterceptor tokenInterceptor() {
		return new TokenInterceptor();
	}

}
