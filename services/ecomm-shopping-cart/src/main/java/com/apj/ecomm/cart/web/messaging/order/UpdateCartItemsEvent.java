package com.apj.ecomm.cart.web.messaging.order;

import java.util.Map;

public record UpdateCartItemsEvent(String buyerId, Map<Long, Integer> products) {

}
