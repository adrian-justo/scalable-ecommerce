package com.apj.ecomm.notification.domain;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.notification.domain.model.EmailMessage;
import com.apj.ecomm.notification.domain.model.Message;
import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.SmsMessage;
import com.apj.ecomm.notification.web.messaging.order.NotificationRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface NotificationMapper {

	NotificationResponse toAudit(Notification entity);

	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	NotificationResponse toResponse(Notification entity);

	Notification toEntity(NotificationRequest request, Message message, boolean successful, String error);

	EmailMessage toEmail(NotificationRequest request, String templateId);

	SmsMessage toSms(NotificationRequest request, String contentSid);

}
