package com.apj.ecomm.account.web.controller;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.domain.IUserService;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.UserNotFoundException;

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
	public List<UserResponse> getAllUsers(@RequestParam(defaultValue = "1") int pageNo,
			@RequestParam(defaultValue = "10") int size) {
		return service.findAll(pageNo, size);
	}

	@GetMapping("/{username}")
	public Optional<UserResponse> getUserByUsername(@PathVariable String username) {
		validate(username);
		return service.findByUsername(username);
	}

	@PutMapping("/{username}")
	public Optional<UserResponse> updateUser(@PathVariable String username,
			@RequestBody @Valid UpdateUserRequest request) {
		validate(username);
		request.validate();
		return service.update(username, request);
	}

	@DeleteMapping("/{username}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteById(@PathVariable String username) {
		validate(username);
		service.deleteByUsername(username);
	}

	private void validate(String username) {
		if (StringUtils.isBlank(username)) {
			throw new UserNotFoundException();
		}
	}

}
