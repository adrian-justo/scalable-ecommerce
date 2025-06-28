package com.apj.ecomm.account.domain;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

	@Value("${secret.key:}")
	private String secretKey;

	private final UserDetailsService service;

	public String generate(User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", user.getRoles());
		return Jwts.builder().claims(claims).subject(user.getUsername()).issuedAt(Date.from(Instant.now()))
				.expiration(Date.from(Instant.now().plus(Duration.ofMinutes(30))))
				.signWith(getSignKey(), Jwts.SIG.HS256).compact();
	}

	private SecretKey getSignKey() {
		if (StringUtils.isBlank(secretKey)) {
			byte[] keyBytes = new byte[32];
			new SecureRandom().nextBytes(keyBytes);
			secretKey = Base64.getEncoder().encodeToString(keyBytes);
			log.warn("Secret key is not configured, generated a random key: {}", secretKey);
			log.warn(
					"To enable JWT signing, configure this key in your API Gateway and ensure it matches the key used in the Auth Service.");
		}
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
