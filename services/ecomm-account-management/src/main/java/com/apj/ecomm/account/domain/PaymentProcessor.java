package com.apj.ecomm.account.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PaymentProcessor {

	String create();

	String getTransferStatus(String accountId);

	boolean transferEnabledFor(String accountId);

	String getDashboardLink(String accountId);

	String getOnboardingLink(String accountId, String refreshUrl);

	void transfer(Map<String, BigDecimal> transferDetails, String paymentIntentId, List<User> users);

}
