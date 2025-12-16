package com.apj.ecomm.account.domain;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.user.payment")
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

	private final IUserService userService;

	private final PaymentProcessor processor;

	public String getPaymentDashboardLink(final String accountId) {
		return processor.getDashboardLink(accountId);
	}

	public String getAccountOnboardingLink(final String accountId, final String refreshUrl) {
		return processor.getOnboardingLink(accountId, refreshUrl);
	}

	public void createTransfer(final Map<String, BigDecimal> transferDetails, final String paymentIntentId) {
		processor.transfer(transferDetails, paymentIntentId, userService.findAllBy(transferDetails.keySet()));
	}

}
