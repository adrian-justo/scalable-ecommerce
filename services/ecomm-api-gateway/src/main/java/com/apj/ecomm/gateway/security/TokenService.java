package com.apj.ecomm.gateway.security;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
class TokenService {

	@Value("${secret.key:}")
	private String secretKey;

	boolean isValid(final String token) {
		Claims payload;
		try {
			payload = getPayload(token);
		}
		catch (final JwtException e) {
			return false;
		}
		return isValid(payload.getExpiration());
	}

	private Claims getPayload(final String token) {
		return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
	}

	private SecretKey getSignKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
	}

	private boolean isValid(final Date expiration) {
		return Date.from(Instant.now()).before(expiration);
	}

	User getUser(final String token) {
		final var claims = getPayload(token);
		return User.builder()
			.username(claims.getSubject())
			.roles((List<String>) claims.get("roles"))
			.shopId((String) claims.get("shopId"))
			.shopName((String) claims.get("shopName"))
			.build();
	}

}
