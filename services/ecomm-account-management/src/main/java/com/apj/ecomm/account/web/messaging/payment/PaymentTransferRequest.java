package com.apj.ecomm.account.web.messaging.payment;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentTransferRequest(Map<String, BigDecimal> transferDetails, String paymentIntentId) {
}