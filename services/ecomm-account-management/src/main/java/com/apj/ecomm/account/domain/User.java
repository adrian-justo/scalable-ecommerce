package com.apj.ecomm.account.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
@DynamicInsert
@DynamicUpdate
class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	private String username;
	private String email;
	private String mobileNo;
	private String password;
	private String name;
	private String shopName;
	private String address;

	@Enumerated(value = EnumType.STRING)
	@ColumnDefault("'{BUYER}'")
	private List<Role> roles;

	@Enumerated(value = EnumType.STRING)
	@ColumnDefault("'{EMAIL}'")
	private List<NotificationType> notificationTypes;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	@Column(columnDefinition = "boolean default true")
	private boolean active = true;

}
