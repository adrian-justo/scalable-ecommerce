package com.apj.ecomm.payment.domain.model;

import com.apj.ecomm.payment.domain.SessionStatus;

public record UpdatePaymentStatus(String buyerId, SessionStatus status) {
}