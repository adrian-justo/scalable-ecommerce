package com.apj.ecomm.account.web.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.domain.IUserService;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.controller.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.user")
@RequiredArgsConstructor
public class UserController {

	private final IUserService service;

	@GetMapping
	public List<UserResponse> getAllUsers(@PageableDefault(page = 0, size = 10) Pageable pageable) {
		return service.findAll(pageable);
	}

	@GetMapping("/{username}")
	public UserResponse getUserByUsername(@PathVariable String username) {
		PathValidator.username(username);
		return service.findByUsername(username);
	}

	@PutMapping("/{username}")
	public UserResponse updateUser(@PathVariable String username, @RequestBody @Valid UpdateUserRequest request) {
		PathValidator.username(username);
		request.validate();
		return service.update(username, request);
	}

	@DeleteMapping("/{username}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteById(@PathVariable String username) {
		PathValidator.username(username);
		service.deleteByUsername(username);
	}

}
