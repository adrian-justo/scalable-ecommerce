package com.apj.ecomm.account.domain;

import java.math.BigDecimal;
import java.util.Map;

public interface IPaymentService {

	String getPaymentDashboardLink(String accountId);

	String getAccountOnboardingLink(String accountId, String refreshUrl);

	void createTransfer(Map<String, BigDecimal> transferDetails, String paymentIntentId);

}
