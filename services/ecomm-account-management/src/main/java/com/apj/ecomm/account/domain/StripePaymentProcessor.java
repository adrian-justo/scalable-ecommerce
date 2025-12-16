package com.apj.ecomm.account.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.LoginLink;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountCreateParams.Capabilities;
import com.stripe.param.AccountCreateParams.Capabilities.Transfers;
import com.stripe.param.AccountCreateParams.Controller;
import com.stripe.param.AccountCreateParams.Controller.Fees;
import com.stripe.param.AccountCreateParams.Controller.Fees.Payer;
import com.stripe.param.AccountCreateParams.Controller.Losses;
import com.stripe.param.AccountCreateParams.Controller.Losses.Payments;
import com.stripe.param.AccountCreateParams.Controller.StripeDashboard;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.LoginLinkCreateOnAccountParams;
import com.stripe.param.TransferCreateParams;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;

@Component
@Observed(name = "processor.user")
class StripePaymentProcessor implements PaymentProcessor {

	@Value("${return.url}")
	private String returnUrl;

	@Value("${stripe.key}")
	private String stripeKey;

	@PostConstruct
	void setKey() {
		Stripe.apiKey = stripeKey;
	}

	public String create() {
		try {
			final var account = Account.create(AccountCreateParams.builder()
				.setController(Controller.builder()
					.setStripeDashboard(StripeDashboard.builder().setType(StripeDashboard.Type.EXPRESS).build())
					.setFees(Fees.builder().setPayer(Payer.APPLICATION).build())
					.setLosses(Losses.builder().setPayments(Payments.APPLICATION).build())
					.build())
				.setCapabilities(
						Capabilities.builder().setTransfers(Transfers.builder().setRequested(true).build()).build())
				.build());
			return account.getId();
		}
		catch (final StripeException e) {
			handle(e);
			return "";
		}
	}

	public String getTransferStatus(final String accountId) {
		try {
			return StringUtils.isBlank(accountId) ? "" : Account.retrieve(accountId).getCapabilities().getTransfers();
		}
		catch (final StripeException e) {
			handle(e);
			return "";
		}
	}

	public boolean transferEnabledFor(final String accountId) {
		return "active".equals(getTransferStatus(accountId));
	}

	public String getDashboardLink(final String accountId) {
		try {
			final var loginLink = LoginLink.createOnAccount(accountId,
					LoginLinkCreateOnAccountParams.builder().build());
			return loginLink.getUrl();
		}
		catch (final StripeException e) {
			handle(e);
			return "Error Occurred. Please try again later.";
		}

	}

	public String getOnboardingLink(final String accountId, final String refreshUrl) {
		try {
			var link = "Account already onboarded.";
			if (!transferEnabledFor(accountId)) {
				final var accountLink = AccountLink.create(AccountLinkCreateParams.builder()
					.setAccount(accountId)
					.setReturnUrl(returnUrl)
					.setRefreshUrl(refreshUrl)
					.setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
					.build());
				link = accountLink.getUrl();
			}
			return link;
		}
		catch (final StripeException e) {
			handle(e);
			return "Error Occurred. Please try again later.";
		}
	}

	private void handle(final StripeException e) {
		e.printStackTrace();
	}

	public void transfer(final Map<String, BigDecimal> transferDetails, final String paymentIntentId,
			final List<User> users) {
		try {
			final var paymentIntent = PaymentIntent.retrieve(paymentIntentId);
			final var deduction = getDeduction(paymentIntent, users.size());
			for (final User user : users) {
				createTransfer(transferDetails, user, deduction, paymentIntent);
			}
		}
		catch (final StripeException e) {
			throw new IllegalStateException(e);
		}
	}

	private BigDecimal getDeduction(final PaymentIntent paymentIntent, final int sellerCount) {
		final var fee = paymentIntent.getLatestChargeObject().getBalanceTransactionObject().getFee() * 3 / 2;
		return BigDecimal.valueOf(fee).divide(BigDecimal.valueOf(sellerCount), RoundingMode.CEILING);
	}

	private Transfer createTransfer(final Map<String, BigDecimal> transferDetails, final User user,
			final BigDecimal deduction, final PaymentIntent paymentIntent) throws StripeException {
		return Transfer.create(TransferCreateParams.builder()
			.setSourceTransaction(paymentIntent.getLatestCharge())
			.setDestination(user.getAccountId())
			.setCurrency(paymentIntent.getCurrency())
			.setAmount(getFinalAmount(transferDetails.get(user.getId().toString()), deduction))
			.setTransferGroup(paymentIntent.getTransferGroup())
			.build());
	}

	private long getFinalAmount(final BigDecimal amount, final BigDecimal deduction) {
		return amount.multiply(BigDecimal.valueOf(100)).subtract(deduction).longValue();
	}

}
