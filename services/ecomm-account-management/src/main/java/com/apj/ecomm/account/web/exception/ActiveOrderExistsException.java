package com.apj.ecomm.account.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ActiveOrderExistsException extends IllegalArgumentException {

	private static final long serialVersionUID = 4444698775558210674L;

}