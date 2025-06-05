package com.apj.ecomm.account.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenService {

	@Value("${secret.key}")
	private String secretKey;

	private final UserDetailsService service;

	public String generate(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", user.getId());
		claims.put("roles", user.getRoles());
		return Jwts.builder().claims(claims).subject(user.getUsername()).issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plus(Duration.ofMinutes(30))))
				.signWith(getSignKey(), Jwts.SIG.HS256).compact();
	}

	private SecretKey getSignKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
	}

	public boolean isValid(String token) {
		Claims payload = getPayload(token);
		return isValid(payload.getSubject(), payload.getExpiration());
	}

	public Claims getPayload(String token) {
		return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
	}

	public boolean isValid(String username, Date expiration) {
		return username.equals(service.loadUserByUsername(username).getUsername())
				&& Date.from(Instant.now()).before(expiration);
	}

}
