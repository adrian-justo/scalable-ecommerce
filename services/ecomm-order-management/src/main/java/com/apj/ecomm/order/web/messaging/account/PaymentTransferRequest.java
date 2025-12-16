package com.apj.ecomm.order.web.messaging.account;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentTransferRequest(Map<String, BigDecimal> transferDetails, String paymentIntentId) {
}