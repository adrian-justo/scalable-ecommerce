package com.apj.ecomm.payment.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.payment.web.messaging.order.OrderItemDetailResponse;
import com.apj.ecomm.payment.web.messaging.order.OrderItemResponse;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams.LineItem;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData;
import com.stripe.param.checkout.SessionListParams.Status;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface StripeMapper {

	Status toStripe(SessionStatus status);

	SessionStatus fromStripe(String status);

	@Mapping(target = "priceData", source = "item.productDetail")
	LineItem toLineItem(OrderItemResponse item);

	@Mapping(target = "productData", source = "detail")
	@Mapping(target = "unitAmountDecimal", source = "detail.price")
	@Mapping(target = "currency", constant = "USD")
	PriceData toPrice(OrderItemDetailResponse detail);

	default ProductData toProduct(final OrderItemDetailResponse detail) {
		return ProductData.builder().setName(detail.name()).addImage(detail.image()).build();
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sessionId", source = "session.id")
	@Mapping(target = "amount", source = "session.amountTotal")
	@Mapping(target = "sessionUrl", source = "session.url")
	Payment toEntity(Session session, String buyerId);

}
