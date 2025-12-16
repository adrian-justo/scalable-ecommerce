package com.apj.ecomm.order.web.messaging.cart;

import java.util.Map;

public record UpdateCartItemsEvent(String buyerId, Map<Long, Integer> products) {

}
