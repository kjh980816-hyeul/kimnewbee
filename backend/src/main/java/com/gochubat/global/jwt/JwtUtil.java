package com.gochubat.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

	private final SecretKey key;
	private final long accessExpiration;
	private final long refreshExpiration;

	public JwtUtil(JwtProperties properties) {
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
		this.accessExpiration = properties.accessExpiration();
		this.refreshExpiration = properties.refreshExpiration();
	}

	public String generateAccessToken(long userId, String role) {
		return buildToken(userId, role, accessExpiration);
	}

	public String generateRefreshToken(long userId) {
		return buildToken(userId, null, refreshExpiration);
	}

	public long getUserId(String token) {
		return Long.parseLong(parse(token).getSubject());
	}

	public String getRole(String token) {
		return parse(token).get("role", String.class);
	}

	public boolean isValid(String token) {
		try {
			parse(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	private String buildToken(long userId, String role, long expirationMillis) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMillis);
		var builder = Jwts.builder()
				.subject(String.valueOf(userId))
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key);
		if (role != null) {
			builder.claim("role", role);
		}
		return builder.compact();
	}

	private Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
