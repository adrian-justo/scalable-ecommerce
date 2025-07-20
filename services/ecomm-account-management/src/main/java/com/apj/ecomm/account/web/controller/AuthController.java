package com.apj.ecomm.account.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.domain.IAuthService;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth/")
@Observed(name = "controller.auth")
@RequiredArgsConstructor
public class AuthController {

	private final IAuthService service;

	@PostMapping("register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@RequestBody @Valid CreateUserRequest request) {
		request.validate();
		return service.register(request);
	}

	@PostMapping("login")
	public String login(@RequestBody @Valid LoginRequest request) {
		return service.login(request);
	}

}
