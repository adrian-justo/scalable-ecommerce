package com.apj.ecomm.payment.web.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.payment.constants.AppConstants;
import com.apj.ecomm.payment.domain.IPaymentService;

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
import lombok.RequiredArgsConstructor;

@Tag(name = "Payment Processing API", description = "Endpoint for viewing payment session")
@RestController
@RequestMapping("${api.version}${payments.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.payment")
@CrossOrigin
@RequiredArgsConstructor
public class PaymentController {

	private final IPaymentService service;

	@Operation(summary = "Payment Session", description = "View your payment session link")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment session" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404",
					description = "Payment session" + AppConstants.MSG_NOT_FOUND
							+ " Ensure you have created a checkout order in Order API.",
					content = @Content) })
	@GetMapping
	public String getPaymentSessionUrl(
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		return service.getSession(buyerId);
	}

}
