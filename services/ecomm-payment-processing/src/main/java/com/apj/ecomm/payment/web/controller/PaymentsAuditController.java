package com.apj.ecomm.payment.web.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.payment.constants.AppConstants;
import com.apj.ecomm.payment.domain.IPaymentsAuditService;
import com.apj.ecomm.payment.domain.model.Paged;
import com.apj.ecomm.payment.domain.model.PaymentResponse;
import com.apj.ecomm.payment.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Payments Audit API", description = "Endpoints for viewing all payments")
@RestController
@RequestMapping("${api.version}${admin.path}${payments.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.admin.payment")
@CrossOrigin
@RequiredArgsConstructor
public class PaymentsAuditController {

	private final IPaymentsAuditService service;

	@Operation(summary = "Payments Audit", description = "View all payments")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payments" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<PaymentResponse> getAllPayments(
			@ParameterObject @PageableDefault(page = 0, size = 10) final Pageable pageable) {
		return service.findAll(pageable);
	}

	@Operation(summary = "Payment Audit Details", description = "View details of a specific payment")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Payment" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Payment" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@GetMapping("/{paymentId}")
	public PaymentResponse getPaymentById(@PathVariable final long paymentId) {
		PathValidator.paymentId(paymentId);
		return service.findById(paymentId);
	}

}
