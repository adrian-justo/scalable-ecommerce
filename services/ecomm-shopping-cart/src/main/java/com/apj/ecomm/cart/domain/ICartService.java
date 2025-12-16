package com.apj.ecomm.cart.domain;

import java.util.List;
import java.util.Map;

import com.apj.ecomm.cart.domain.model.CartDetailResponse;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;

public interface ICartService {

	CartDetailResponse findCartBy(String buyerId);

	void createCart(String buyerId);

	List<CartItemResponse> findItemsBy(String buyerId);

	CartItemDetail findItemBy(long productId, String buyerId);

	List<CartItemResponse> addItems(String buyerId, List<CartItemRequest> requestList);

	List<CartItemResponse> updateItems(String buyerId, List<CartItemRequest> requestList);

	List<CartItemResponse> updateItemsFromEvent(String buyerId, Map<Long, Integer> products);

	void deleteItems(String buyerId, List<Long> productIds);

	void updateCartOrdered(String buyerId);

}
