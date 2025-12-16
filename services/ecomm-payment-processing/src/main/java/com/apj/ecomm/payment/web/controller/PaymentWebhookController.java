package com.apj.ecomm.payment.web.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.payment.domain.IPaymentWebhookService;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequestMapping("${api.version}${webhooks.path}${payments.path}")
@Observed(name = "controller.webhook.payment")
@CrossOrigin
@RequiredArgsConstructor
public class PaymentWebhookController {

	private final IPaymentWebhookService service;

	@PostMapping
	public void handleWebhookEvent(@RequestBody final String payload, @RequestHeader final HttpHeaders headers) {
		service.handleEvent(payload, headers);
	}

}