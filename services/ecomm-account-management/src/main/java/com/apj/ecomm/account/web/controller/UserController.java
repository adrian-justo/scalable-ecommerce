package com.apj.ecomm.account.web.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.IUserService;
import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.util.PathValidator;
import com.apj.ecomm.account.web.util.RequestValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Account API", description = "Endpoints for viewing of account details and account management")
@RestController
@RequestMapping("${api.version}${users.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.user")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

	private final IUserService service;

	@Operation(summary = "Accounts Audit",
			description = "View details of all accounts. Only an administrator can access this endpoint")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Accounts" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<UserResponse> getAllUsers(
			@ParameterObject @PageableDefault(page = 0, size = 10) final Pageable pageable) {
		return service.findAll(pageable);
	}

	@Operation(summary = "Account Details", description = "View details of a user account")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Account" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Account" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@GetMapping("/{username}")
	public UserResponse getUserByUsername(@PathVariable final String username,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String userId) {
		PathValidator.username(username);
		return service.findByUsername(username, userId);
	}

	@Operation(summary = "Account Management", description = "Update details of a user account")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Account updated successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Account" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@PutMapping("/{username}")
	public UserResponse updateUser(@PathVariable final String username,
			@RequestBody @Valid final UpdateUserRequest request) {
		PathValidator.username(username);
		RequestValidator.validate(request);
		return service.update(username, request);
	}

	@Operation(summary = "Account Deletion", description = "Delete a user account")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Account" + AppConstants.MSG_NOT_FOUND,
					content = @Content),
			@ApiResponse(responseCode = "422", description = AppConstants.MSG_UNPROCESSABLE_ENTITY,
					content = @Content) })
	@DeleteMapping("/{username}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@PathVariable final String username) {
		PathValidator.username(username);
		service.deleteByUsername(username);
	}

}
