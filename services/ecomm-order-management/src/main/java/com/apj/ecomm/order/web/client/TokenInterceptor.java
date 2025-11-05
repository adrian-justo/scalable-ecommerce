package com.apj.ecomm.order.web.client;

import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class TokenInterceptor implements RequestInterceptor {

	@Override
	public void apply(final RequestTemplate template) {
		final var requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			template.header(HttpHeaders.AUTHORIZATION,
					requestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION));
		}
	}

}
