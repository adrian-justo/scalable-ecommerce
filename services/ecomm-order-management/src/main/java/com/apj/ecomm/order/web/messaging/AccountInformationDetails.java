package com.apj.ecomm.order.web.messaging;

import java.util.Map;

public record AccountInformationDetails(String buyerId, Map<String, UserResponse> users) {
}