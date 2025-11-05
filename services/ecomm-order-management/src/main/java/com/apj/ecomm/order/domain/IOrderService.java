package com.apj.ecomm.order.domain;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.order.domain.model.DeliveryInformationRequest;
import com.apj.ecomm.order.domain.model.OrderResponse;
import com.apj.ecomm.order.domain.model.Paged;
import com.apj.ecomm.order.web.messaging.ProductResponse;
import com.apj.ecomm.order.web.messaging.ProductStockUpdate;
import com.apj.ecomm.order.web.messaging.UserResponse;

public interface IOrderService {

	Paged<OrderResponse> findAllBy(String buyerId, Pageable pageable);

	OrderResponse findById(long id, String buyerId);

	List<OrderResponse> checkOut(String buyerId, DeliveryInformationRequest request);

	ProductStockUpdate updateInformationAndGetStockUpdate(String buyerId, Map<String, UserResponse> userInformation);

	List<OrderItem> populateOrderItemDetail(String buyerId, Map<Long, ProductResponse> details);

}
