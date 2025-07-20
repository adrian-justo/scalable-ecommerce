package com.apj.ecomm.gateway.security;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.apj.ecomm.gateway.security.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {

	@Value("${secret.key:}")
	private String secretKey;

	public boolean isValid(String token) {
		Claims payload = getPayload(token);
		return isValid(payload.getExpiration());
	}

	public Claims getPayload(String token) {
		return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
	}

	private SecretKey getSignKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
	}

	public boolean isValid(Date expiration) {
		return Date.from(Instant.now()).before(expiration);
	}

	public User getUser(String token) {
		Claims claims = getPayload(token);
		return User.builder().username(claims.getSubject()).roles((List<String>) claims.get("roles"))
				.shopName((String) claims.get("shopName")).build();
	}

}
