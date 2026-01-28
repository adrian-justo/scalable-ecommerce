package com.apj.ecomm.notification.domain.model;

import org.springframework.data.annotation.TypeAlias;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@TypeAlias(Message.ALIAS_SMS)
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SmsMessage extends Message {

	private String contentSid;

}
