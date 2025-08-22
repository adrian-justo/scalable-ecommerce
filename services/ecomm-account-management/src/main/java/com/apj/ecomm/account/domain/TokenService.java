package com.apj.ecomm.account.domain;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class TokenService {

	@Value("${secret.key:}")
	private String secretKey;

	String generate(final User user) {
		final var claims = new HashMap<String, Object>();
		claims.put("roles", user.getRoles());
		claims.put("userId", user.getId().toString());
		claims.put("shopName", user.getShopName());
		// Any other claims to be used as request header must also be added in
		// AuthFilter class of API Gateway
		return Jwts.builder()
			.claims(claims)
			.subject(user.getUsername())
			.issuedAt(Date.from(Instant.now()))
			.expiration(Date.from(Instant.now().plus(Duration.ofMinutes(30))))
			.signWith(getSignKey(), Jwts.SIG.HS256)
			.compact();
	}

	private SecretKey getSignKey() {
		if (StringUtils.isBlank(secretKey)) {
			secretKey = generateSecret();
			log.warn("Secret key is not configured, generated a random key: {}", secretKey);
			log.warn("To enable JWT signing, configure this key in your API Gateway"
					+ " and ensure it matches the key used in the Account Management Service.");
		}
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
	}

	String generateSecret() {
		final var keyBytes = new byte[32];
		new SecureRandom().nextBytes(keyBytes);
		return Hex.encodeHexString(keyBytes).toUpperCase();
	}

}
