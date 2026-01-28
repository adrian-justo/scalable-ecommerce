package com.apj.ecomm.order.web.messaging;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.order.domain.IOrderService;
import com.apj.ecomm.order.web.messaging.account.AccountInformationDetails;
import com.apj.ecomm.order.web.messaging.account.PaymentTransferRequest;
import com.apj.ecomm.order.web.messaging.payment.CheckoutSessionRequest;
import com.apj.ecomm.order.web.messaging.payment.UpdateOrderStatusEvent;
import com.apj.ecomm.order.web.messaging.product.OrderedProductDetails;
import com.apj.ecomm.order.web.messaging.product.ProductStockUpdate;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OrderMessageConsumer {

	private final IOrderService service;

	@Bean
	Function<AccountInformationDetails, ProductStockUpdate> updateInformationAndSendStockUpdate() {
		return data -> service.updateInformationAndGetProducts(data.buyerId(), data.users())
			.map(products -> new ProductStockUpdate(data.buyerId(), products))
			.orElse(null);
	}

	@Bean
	Function<OrderedProductDetails, CheckoutSessionRequest> populateDetailAndRequestCheckout() {
		return data -> service.populateDetailAndGetOrders(data.buyerId(), data.details())
			.map(CheckoutSessionRequest::new)
			.orElse(null);
	}

	@Bean
	Function<UpdateOrderStatusEvent, PaymentTransferRequest> updateStatusAndRequestTransfer() {
		return data -> service.updateStatusAndGetDetails(data.buyerId(), data.status())
			.map(transferDetails -> new PaymentTransferRequest(transferDetails, data.paymentIntentId()))
			.orElse(null);
	}

}
