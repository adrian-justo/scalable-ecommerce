package com.apj.ecomm.account.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
class User implements UserDetails {

	private static final long serialVersionUID = 2152803822016817984L;

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String mobileNo;

	private String password;

	private String name;

	private String shopName;

	private String address;

	@Enumerated(value = EnumType.STRING)
	@ColumnDefault("'{BUYER}'")
	private Set<Role> roles;

	@Enumerated(value = EnumType.STRING)
	private Set<NotificationType> notificationTypes;

	private String accountId;

	@CreationTimestamp
	private Instant createdAt;

	@UpdateTimestamp
	private Instant updatedAt;

	@Column(columnDefinition = "boolean default true")
	private boolean active = true;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).toList();
	}

}
