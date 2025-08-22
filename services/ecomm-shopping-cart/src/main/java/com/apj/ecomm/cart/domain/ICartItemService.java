package com.apj.ecomm.cart.domain;

import java.util.List;

import com.apj.ecomm.cart.domain.model.CartItemCatalog;
import com.apj.ecomm.cart.domain.model.CartItemDetail;
import com.apj.ecomm.cart.domain.model.CartItemRequest;
import com.apj.ecomm.cart.domain.model.CartItemResponse;

public interface ICartItemService extends ICartClientService {

	List<CartItemCatalog> findAll(long cartId, String buyerId);

	CartItemDetail findById(long cartId, String buyerId, long productId);

	List<CartItemResponse> addAll(long cartId, String buyerId, List<CartItemRequest> requestList);

	List<CartItemResponse> updateAll(long cartId, String buyerId, List<CartItemRequest> requestList);

	void deleteAll(long cartId, String buyerId, List<Long> productIds);

}
