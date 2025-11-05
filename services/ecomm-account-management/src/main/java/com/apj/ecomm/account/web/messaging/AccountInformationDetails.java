package com.apj.ecomm.account.web.messaging;

import java.util.Map;

import com.apj.ecomm.account.domain.model.UserResponse;

public record AccountInformationDetails(String buyerId, Map<String, UserResponse> users) {
}