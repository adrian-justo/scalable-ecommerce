package com.apj.ecomm.account.web.messaging.order;

import java.util.Set;

public record RequestAccountInformationEvent(String buyerId, Set<String> userIds) {
}