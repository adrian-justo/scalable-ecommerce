package com.apj.ecomm.payment.domain;

import org.springframework.http.HttpHeaders;

public interface IPaymentWebhookService {

	void handleEvent(String payload, HttpHeaders headers);

}
