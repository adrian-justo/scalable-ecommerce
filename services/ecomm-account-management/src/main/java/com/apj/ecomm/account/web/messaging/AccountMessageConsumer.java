package com.apj.ecomm.account.web.messaging;

import java.util.UUID;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.account.domain.IUserService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AccountMessageConsumer {

	private final IUserService service;

	@Bean
	Function<RequestAccountInformationEvent, AccountInformationDetails> returnUserDetails() {
		return data -> new AccountInformationDetails(data.buyerId(),
				service.findAllBy(data.userIds().stream().map(UUID::fromString).toList()));
	}

}
