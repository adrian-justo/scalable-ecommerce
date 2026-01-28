package com.apj.ecomm.order.domain;

import java.util.Set;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.apj.ecomm.order.web.messaging.account.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
class ShopInformation {

	@Id
	private Long id;

	private String name;

	private String address;

	private String email;

	private String mobileNo;

	@Enumerated(value = EnumType.STRING)
	private Set<NotificationType> notificationTypes;

	@JsonIgnore
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToOne
	@MapsId
	private Order order;

	String getRecipientBy(final NotificationType type) {
		return switch (type) {
			case EMAIL -> email;
			case SMS -> mobileNo;
			default -> throw new IllegalArgumentException("Unexpected value: " + type);
		};
	}

}
