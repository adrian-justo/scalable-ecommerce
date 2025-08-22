package com.apj.ecomm.cart.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;

import com.apj.ecomm.cart.web.client.CartClient;
import com.apj.ecomm.cart.web.client.product.ProductCatalog;

public interface ICartClientService {

	default List<ProductCatalog> getProductsFrom(final CartClient client, final List<CartItem> items) {
		return getProductsBy(items.stream().map(CartItem::getProductId), client);
	}

	default List<ProductCatalog> getProductsBy(final Stream<Long> stream, final CartClient client) {
		final var productIds = stream.sorted().toList();
		return productIds.isEmpty() ? List.of()
				: client
					.getAllProducts("id:" + productIds.stream().map(String::valueOf).collect(Collectors.joining(",")),
							PageRequest.ofSize(productIds.size()))
					.result();
	}

}
