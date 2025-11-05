package com.apj.ecomm.account.web.messaging;

import java.util.Set;

public record RequestAccountInformationEvent(String buyerId, Set<String> userIds) {
}