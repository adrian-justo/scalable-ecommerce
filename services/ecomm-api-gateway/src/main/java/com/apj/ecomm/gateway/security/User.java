package com.apj.ecomm.gateway.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class User implements UserDetails {

	private static final long serialVersionUID = -8563495116816209640L;

	private String username;

	private String password;

	private List<String> roles;

	private String userId;

	private String shopName;

	private String transferStatus;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
	}

}
