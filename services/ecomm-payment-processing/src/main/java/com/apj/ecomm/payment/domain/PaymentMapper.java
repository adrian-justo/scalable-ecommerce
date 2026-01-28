package com.apj.ecomm.payment.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.payment.domain.model.PaymentResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface PaymentMapper {

	@Mapping(target = "sessionId", ignore = true)
	@Mapping(target = "sessionUrl", ignore = true)
	PaymentResponse toAudit(Payment entity);

	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	PaymentResponse toResponse(Payment entity);

}
