package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Account.Capabilities;
import com.stripe.model.AccountLink;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import com.stripe.model.LoginLink;
import com.stripe.model.PaymentIntent;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.LoginLinkCreateOnAccountParams;

@Disabled
@ExtendWith(MockitoExtension.class)
class StripePaymentProcessorTest {

	private static final String STRIPE_MOCK = "http://localhost:12111";

	private static final User USER = new User();

	@InjectMocks
	private StripePaymentProcessor processor;

	@Mock
	private Account account;

	@Mock
	private Capabilities capabilities;

	@Mock
	private LoginLink loginLink;

	@Mock
	private AccountLink accountLink;

	@Mock
	private PaymentIntent paymentIntent;

	@Mock
	private Charge charge;

	@Mock
	private BalanceTransaction balanceTransaction;

	@BeforeAll
	static void setUpBeforeClass() {
		Stripe.overrideApiBase(STRIPE_MOCK);
		USER.setId(UUID.randomUUID());
		USER.setAccountId("acct_123");
	}

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(processor, "stripeKey", "sk_test_123");
		ReflectionTestUtils.setField(processor, "returnUrl", STRIPE_MOCK + "/return");
		processor.setKey();
	}

	@Test
	void create() throws StripeException {
		final var acct = Account.retrieve(USER.getAccountId());
		try (final var mock = mockStatic(Account.class)) {
			mock.when(() -> Account.create(any(AccountCreateParams.class))).thenReturn(acct);
			assertEquals(USER.getAccountId(), processor.create());
		}
	}

	@Test
	void getTransferStatus() {
		final var transferStatus = "transferStatus";
		try (final var mock = mockStatic(Account.class)) {
			mock.when(() -> Account.retrieve(anyString())).thenReturn(account);
			when(account.getCapabilities()).thenReturn(capabilities);
			when(capabilities.getTransfers()).thenReturn(transferStatus);
			assertEquals(transferStatus, processor.getTransferStatus(USER.getAccountId()));
		}
	}

	@Test
	void transferEnabledFor() {
		try (final var mock = mockStatic(Account.class)) {
			mock.when(() -> Account.retrieve(anyString())).thenReturn(account);
			when(account.getCapabilities()).thenReturn(capabilities);
			when(capabilities.getTransfers()).thenReturn("active");
			assertTrue(processor.transferEnabledFor(USER.getAccountId()));
		}
	}

	@Test
	void getDashboardLink() {
		final var link = "dashboardLink";
		try (final var mock = mockStatic(LoginLink.class)) {
			mock.when(() -> LoginLink.createOnAccount(anyString(), any(LoginLinkCreateOnAccountParams.class)))
				.thenReturn(loginLink);
			when(loginLink.getUrl()).thenReturn(link);
			assertEquals(link, processor.getDashboardLink(USER.getAccountId()));
		}
	}

	@Test
	void getOnboardingLink() {
		final var link = "onboardingLink";
		try (final var acct = mockStatic(Account.class); final var mock = mockStatic(AccountLink.class)) {
			acct.when(() -> Account.retrieve(anyString())).thenReturn(account);
			when(account.getCapabilities()).thenReturn(capabilities);
			when(capabilities.getTransfers()).thenReturn("inactive");
			mock.when(() -> AccountLink.create(any(AccountLinkCreateParams.class))).thenReturn(accountLink);
			when(accountLink.getUrl()).thenReturn(link);
			assertEquals(link, processor.getOnboardingLink(USER.getAccountId(), STRIPE_MOCK + "/refresh"));
		}
	}

	@Test
	void transfer() {
		try (final var intent = mockStatic(PaymentIntent.class)) {
			intent.when(() -> PaymentIntent.retrieve(anyString())).thenReturn(paymentIntent);
			when(paymentIntent.getLatestChargeObject()).thenReturn(charge);
			when(charge.getBalanceTransactionObject()).thenReturn(balanceTransaction);
			when(balanceTransaction.getFee()).thenReturn(32L);
			when(paymentIntent.getCurrency()).thenReturn("usd");

			processor.transfer(Map.of(USER.getId().toString(), BigDecimal.ONE), "pi_123", List.of(USER));
			// verify call in stripe-mock
		}
	}

}
