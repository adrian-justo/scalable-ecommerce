package com.apj.ecomm.account.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	private User user;

	@Mock
	private IUserService userService;

	@Mock
	private PaymentProcessor processor;

	@InjectMocks
	private PaymentService service;

	@BeforeEach
	void setUp() throws Exception {
		final var objMap = new ObjectMapper();
		objMap.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (var inputStream = TypeReference.class.getResourceAsStream("/data/users.json")) {
			final var users = objMap.readValue(inputStream, new TypeReference<List<User>>() {
			});
			user = users.get(1);
			user.setId(UUID.randomUUID());
		}
	}

	@Test
	void getPaymentDashboardLink() {
		final var link = "paymentDashboardLink";
		when(processor.getDashboardLink(anyString())).thenReturn(link);
		assertEquals(link, service.getPaymentDashboardLink(user.getAccountId()));
	}

	@Test
	void getAccountOnboardingLink() {
		final var link = "accountOnboardingLink";
		when(processor.getOnboardingLink(anyString(), anyString())).thenReturn(link);
		assertEquals(link, service.getAccountOnboardingLink(user.getAccountId(), "refreshUrl"));
	}

	@Test
	void createTransfer() {
		final var transferDetails = Map.of(user.getId().toString(), BigDecimal.ONE);
		final var paymentIntentId = "paymentIntentId";

		when(userService.findAllBy(ArgumentMatchers.<Set<String>>any())).thenReturn(List.of(user));

		service.createTransfer(transferDetails, paymentIntentId);
		verify(processor, times(1)).transfer(transferDetails, paymentIntentId, List.of(user));
	}

}
