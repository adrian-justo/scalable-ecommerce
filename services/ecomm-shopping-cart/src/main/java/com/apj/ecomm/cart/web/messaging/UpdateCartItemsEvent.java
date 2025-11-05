package com.apj.ecomm.cart.web.messaging;

import java.util.Map;

public record UpdateCartItemsEvent(String buyerId, Map<Long, Integer> products) {

}
