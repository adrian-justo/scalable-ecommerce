package com.apj.ecomm.order.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.order.domain.model.DeliveryInformationRequest;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.messaging.account.UserResponse;
import com.apj.ecomm.order.web.messaging.payment.CheckoutSessionRequest;
import com.apj.ecomm.order.web.messaging.product.ProductResponse;
import com.apj.ecomm.order.web.messaging.product.ProductStockUpdate;

public interface IOrderService {

	Paged<OrderResponse> findAllBy(String buyerId, Pageable pageable);

	OrderResponse findById(long id, String buyerId);

	List<OrderResponse> checkOut(String buyerId, DeliveryInformationRequest request);

	ProductStockUpdate updateInformationAndGetStockUpdate(String buyerId, Map<String, UserResponse> userInformation);

	CheckoutSessionRequest populateDetailAndRequestCheckout(String buyerId, Map<Long, ProductResponse> details);

	Map<String, BigDecimal> updateStatusAndGetDetails(String buyerId, Status status);

}
