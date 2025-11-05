package com.apj.ecomm.order.web.messaging;

import java.util.Set;

public record RequestAccountInformationEvent(String buyerId, Set<String> userIds) {
}