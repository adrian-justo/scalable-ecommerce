package com.apj.ecomm.product.web.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apj.ecomm.product.domain.IProductService;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductFromMessageRequest;
import com.apj.ecomm.product.web.messaging.account.ShopNameUpdatedEvent;
import com.apj.ecomm.product.web.messaging.account.ShopStatusUpdatedEvent;
import com.apj.ecomm.product.web.messaging.order.OrderedProductDetails;
import com.apj.ecomm.product.web.messaging.order.ProductStockUpdate;
import com.apj.ecomm.product.web.messaging.order.ReturnProductStockEvent;
import com.apj.ecomm.product.web.util.Executor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ProductMessageConsumer {

	private final IProductService service;

	private final Executor executor;

	@Bean
	Consumer<ShopNameUpdatedEvent> syncShopName() {
		// Two phase query used to ensure write-through cache consistency
		return data -> service.getProductsBy(data.shopId())
			.forEach(id -> executor.lockFor(id,
					() -> service.update(id, new UpdateProductFromMessageRequest(data.shopName()))));
	}

	@Bean
	Function<ProductStockUpdate, OrderedProductDetails> processAndReturnDetail() {
		return data -> new OrderedProductDetails(data.buyerId(), processAndReturnDetail(data.products()));
	}

	private Map<Long, ProductResponse> processAndReturnDetail(final Map<Long, Integer> products) {
		final var preservedDetails = new HashMap<Long, ProductResponse>();
		products.forEach((id, quantity) -> executor.lockFor(id, () -> {
			final var product = service.findById(id);
			preservedDetails.put(id, product);
			if (product.stock() > 0) {
				service.update(id, new UpdateProductFromMessageRequest(quantity, true));
			}
		}));
		return preservedDetails;
	}

	@Bean
	Consumer<ReturnProductStockEvent> returnProductStock() {
		return data -> data.products()
			.forEach((id, quantity) -> executor.lockFor(id,
					() -> service.update(id, new UpdateProductFromMessageRequest(quantity, false))));
	}

	@Bean
	Consumer<ShopStatusUpdatedEvent> syncShopStatus() {
		return data -> service.getProductsBy(data.shopId(), !data.active())
			.forEach(id -> executor.lockFor(id,
					() -> service.update(id, new UpdateProductFromMessageRequest(data.active()))));
	}

}
