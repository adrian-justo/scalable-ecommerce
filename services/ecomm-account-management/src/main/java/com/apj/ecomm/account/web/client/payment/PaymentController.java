package com.apj.ecomm.account.web.client.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.domain.IPaymentService;
import com.apj.ecomm.account.domain.IUserService;
import com.apj.ecomm.account.domain.Role;
import com.apj.ecomm.account.web.util.AccessValidator;
import com.apj.ecomm.account.web.util.PathValidator;

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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Tag(name = "My Payments",
		description = "Endpoints for viewing your payment dashboard. Only a seller can access this endpoint")
@RestController
@RequestMapping("${api.version}${users.path}/{username}${payments.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.user.payment")
@CrossOrigin
@RequiredArgsConstructor
public class PaymentController {

	@Value("${gateway.url}")
	private String baseUrl;

	private final IUserService userService;

	private final IPaymentService service;

	@Operation(summary = "Payment Dashboard", description = "View my payment dashboard")
	@ApiResponses(
			value = { @ApiResponse(responseCode = "200", description = "Payment Dashboard Link" + AppConstants.MSG_OK),
					@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
					@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
					@ApiResponse(responseCode = "404",
							description = "Account" + AppConstants.MSG_NOT_FOUND + " / "
									+ AppConstants.MSG_ACCESS_DENIED + "endpoint",
							content = @Content) })
	@GetMapping
	public String getPaymentDashboardLink(@PathVariable final String username,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId) {
		PathValidator.username(username);
		final var user = userService.findByUsername(username, shopId);
		AccessValidator.hasRole(Role.SELLER, user);
		return service.getPaymentDashboardLink(user.accountId());
	}

	@Operation(summary = "Account Onboarding", description = "View link to onboard account for payouts")
	@ApiResponses(
			value = { @ApiResponse(responseCode = "200", description = "Account Onboarding Link" + AppConstants.MSG_OK),
					@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
					@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
					@ApiResponse(responseCode = "404",
							description = "Account" + AppConstants.MSG_NOT_FOUND + " / "
									+ AppConstants.MSG_ACCESS_DENIED + "endpoint",
							content = @Content) })
	@GetMapping("${onboarding.path}")
	public String getAccountOnboardingLink(@PathVariable final String username,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String shopId,
			final HttpServletRequest request) {
		PathValidator.username(username);
		final var user = userService.findByUsername(username, shopId);
		AccessValidator.hasRole(Role.SELLER, user);
		return service.getAccountOnboardingLink(user.accountId(), baseUrl + request.getRequestURI());
	}

}
