package com.apj.ecomm.account.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.IAuthService;
import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.util.RequestValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Authentication API", description = "Endpoints for account creation and login")
@RestController
@RequestMapping("${api.version}${auth.path}")
@Observed(name = "controller.auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

	private final IAuthService service;

	@Operation(summary = "User Registration", description = "Create a user account")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Account registered and returned"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "409", description = AppConstants.MSG_CONFLICT, content = @Content) })
	@PostMapping("register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@RequestBody @Valid final CreateUserRequest request) {
		RequestValidator.validate(request);
		return service.register(request);
	}

	@Operation(summary = "Login", description = "Login my account")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Login successful and token returned"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "401", description = AppConstants.MSG_UNAUTHORIZED, content = @Content) })
	@PostMapping("login")
	public String login(@RequestBody @Valid final LoginRequest request) {
		return service.login(request);
	}

}
