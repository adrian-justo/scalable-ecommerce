package com.apj.ecomm.cart.domain;

import java.util.List;
import java.util.stream.Stream;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.cart.domain.model.CartDetailResponse;
import com.apj.ecomm.cart.domain.model.CartResponse;
import com.apj.ecomm.cart.web.client.product.ProductResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = CartItemMapper.class)
interface CartMapper {

	@Mapping(target = "products", qualifiedByName = "toFullResponse")
	CartResponse toResponse(Cart cart);

	CartDetailResponse toDetail(Cart cart, @Context Stream<ProductResponse> products);

	Cart create(String buyerId, List<CartItem> products);

}
