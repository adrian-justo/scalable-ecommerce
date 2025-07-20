package com.apj.ecomm.account.web.messaging;

import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class TokenInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		if (requestAttributes != null) {
			template.header(HttpHeaders.AUTHORIZATION,
					requestAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION));
		}
	}

}
