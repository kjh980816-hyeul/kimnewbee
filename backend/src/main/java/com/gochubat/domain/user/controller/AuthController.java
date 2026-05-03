package com.gochubat.domain.user.controller;

import com.gochubat.domain.user.service.AuthService;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import com.gochubat.global.jwt.JwtProperties;
import com.gochubat.global.oauth.NaverOauthClient;
import com.gochubat.global.oauth.NaverOauthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	static final String COOKIE_ACCESS = "access_token";
	static final String COOKIE_REFRESH = "refresh_token";
	static final String COOKIE_OAUTH_STATE = "oauth_state";
	private static final long OAUTH_STATE_TTL_SECONDS = 300L;

	private final AuthService authService;
	private final NaverOauthClient naverOauthClient;
	private final NaverOauthProperties naverProperties;
	private final JwtProperties jwtProperties;

	public AuthController(
			AuthService authService,
			NaverOauthClient naverOauthClient,
			NaverOauthProperties naverProperties,
			JwtProperties jwtProperties
	) {
		this.authService = authService;
		this.naverOauthClient = naverOauthClient;
		this.naverProperties = naverProperties;
		this.jwtProperties = jwtProperties;
	}

	@GetMapping("/naver/login")
	public ResponseEntity<Void> startNaverLogin(HttpServletResponse response) {
		String state = naverOauthClient.generateState();
		response.addHeader(HttpHeaders.SET_COOKIE, buildStateCookie(state).toString());
		URI authorizeUri = naverOauthClient.buildAuthorizeUri(state);
		return ResponseEntity.status(HttpStatus.FOUND).location(authorizeUri).build();
	}

	@GetMapping("/naver/callback")
	public ResponseEntity<Void> naverCallback(
			@RequestParam("code") String code,
			@RequestParam("state") String state,
			HttpServletRequest request,
			HttpServletResponse response
	) {
		String savedState = readCookie(request, COOKIE_OAUTH_STATE);
		if (savedState == null || !savedState.equals(state)) {
			throw new CustomException(ErrorCode.OAUTH_STATE_MISMATCH);
		}

		AuthService.IssuedTokens tokens = authService.loginWithNaver(code, state);
		response.addHeader(HttpHeaders.SET_COOKIE, expireStateCookie().toString());
		response.addHeader(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString());
		response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString());
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(naverProperties.successRedirectUrl())).build();
	}

	@PostMapping("/refresh")
	public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = readCookie(request, COOKIE_REFRESH);
		if (refreshToken == null) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
		AuthService.IssuedTokens tokens = authService.refresh(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, buildAccessCookie(tokens.accessToken()).toString());
		response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken()).toString());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
		response.addHeader(HttpHeaders.SET_COOKIE, expireCookie(COOKIE_ACCESS).toString());
		response.addHeader(HttpHeaders.SET_COOKIE, expireCookie(COOKIE_REFRESH).toString());
		return ResponseEntity.noContent().build();
	}

	private ResponseCookie buildAccessCookie(String token) {
		return ResponseCookie.from(COOKIE_ACCESS, token)
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/")
				.maxAge(jwtProperties.accessExpiration() / 1000)
				.build();
	}

	private ResponseCookie buildRefreshCookie(String token) {
		return ResponseCookie.from(COOKIE_REFRESH, token)
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/")
				.maxAge(jwtProperties.refreshExpiration() / 1000)
				.build();
	}

	private ResponseCookie buildStateCookie(String state) {
		return ResponseCookie.from(COOKIE_OAUTH_STATE, state)
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/api/auth")
				.maxAge(OAUTH_STATE_TTL_SECONDS)
				.build();
	}

	private ResponseCookie expireStateCookie() {
		return ResponseCookie.from(COOKIE_OAUTH_STATE, "")
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/api/auth")
				.maxAge(0)
				.build();
	}

	private ResponseCookie expireCookie(String name) {
		return ResponseCookie.from(name, "")
				.httpOnly(true)
				.secure(false)
				.sameSite("Lax")
				.path("/")
				.maxAge(0)
				.build();
	}

	private String readCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(name)) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
