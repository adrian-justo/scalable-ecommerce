package com.apj.ecomm.cart.domain;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.cart.domain.model.BuyerCartResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.web.client.product.ProductCatalog;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = CartItemMapper.class)
interface CartMapper {

	CartResponse toResponse(Cart cart);

	BuyerCartResponse toDetail(Cart cart, @Context List<ProductCatalog> products);

	Cart create(String buyerId, List<CartItem> products);

}
