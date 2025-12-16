package com.apj.ecomm.order.web.messaging.account;

import java.util.Map;

public record AccountInformationDetails(String buyerId, Map<String, UserResponse> users) {
}