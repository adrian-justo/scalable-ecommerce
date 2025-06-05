package com.apj.ecomm.account.domain.model;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Please input your Username/Email address/Mobile no.") String identifier,
		@NotBlank(message = "Please input your password") String password) {

}
