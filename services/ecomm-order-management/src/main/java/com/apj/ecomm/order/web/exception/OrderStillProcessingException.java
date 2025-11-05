package com.apj.ecomm.order.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderStillProcessingException extends RuntimeException {

	private static final long serialVersionUID = -8374145865213749490L;

}