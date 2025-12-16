package com.apj.ecomm.account.web.messaging;

import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.account.domain.IPaymentService;
import com.apj.ecomm.account.domain.IUserService;
import com.apj.ecomm.account.web.messaging.order.AccountInformationDetails;
import com.apj.ecomm.account.web.messaging.order.RequestAccountInformationEvent;
import com.apj.ecomm.account.web.messaging.payment.PaymentTransferRequest;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AccountMessageConsumer {

	private final IUserService user;

	private final IPaymentService payment;

	@Bean
	Function<RequestAccountInformationEvent, AccountInformationDetails> returnUserDetails() {
		return data -> new AccountInformationDetails(data.buyerId(), user.getDetails(data.userIds()));
	}

	@Bean
	Consumer<PaymentTransferRequest> createPaymentTransfer() {
		return data -> payment.createTransfer(data.transferDetails(), data.paymentIntentId());
	}

}
