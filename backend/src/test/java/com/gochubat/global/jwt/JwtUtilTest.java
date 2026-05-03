package com.gochubat.global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

	private static final String SECRET = "test-secret-DO-NOT-USE-IN-PRODUCTION-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa==";

	private JwtUtil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil(new JwtProperties(SECRET, 60_000L, 600_000L));
	}

	@Test
	void access_token_round_trip() {
		String token = jwtUtil.generateAccessToken(42L, "PEPPER");

		assertThat(jwtUtil.isValid(token)).isTrue();
		assertThat(jwtUtil.getUserId(token)).isEqualTo(42L);
		assertThat(jwtUtil.getRole(token)).isEqualTo("PEPPER");
	}

	@Test
	void refresh_token_has_no_role() {
		String token = jwtUtil.generateRefreshToken(7L);

		assertThat(jwtUtil.isValid(token)).isTrue();
		assertThat(jwtUtil.getUserId(token)).isEqualTo(7L);
		assertThat(jwtUtil.getRole(token)).isNull();
	}

	@Test
	void invalid_token_returns_false() {
		assertThat(jwtUtil.isValid("not.a.token")).isFalse();
		assertThat(jwtUtil.isValid("")).isFalse();
	}

	@Test
	void expired_token_returns_false() throws InterruptedException {
		JwtUtil shortLived = new JwtUtil(new JwtProperties(SECRET, 1L, 1L));
		String token = shortLived.generateAccessToken(1L, "SEED");
		Thread.sleep(20L);

		assertThat(shortLived.isValid(token)).isFalse();
	}
}
