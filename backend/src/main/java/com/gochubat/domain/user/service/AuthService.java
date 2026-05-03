package com.gochubat.domain.user.service;

import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import com.gochubat.global.jwt.JwtUtil;
import com.gochubat.global.oauth.NaverOauthClient;
import com.gochubat.global.oauth.NaverTokenResponse;
import com.gochubat.global.oauth.NaverUserInfoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final NaverOauthClient naverOauthClient;
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;

	public AuthService(NaverOauthClient naverOauthClient, UserRepository userRepository, JwtUtil jwtUtil) {
		this.naverOauthClient = naverOauthClient;
		this.userRepository = userRepository;
		this.jwtUtil = jwtUtil;
	}

	@Transactional
	public IssuedTokens loginWithNaver(String code, String state) {
		NaverTokenResponse token = naverOauthClient.exchangeCodeForToken(code, state);
		NaverUserInfoResponse.Profile profile = naverOauthClient.fetchUserInfo(token.accessToken()).response();

		User user = userRepository.findByNaverId(profile.id())
				.map(existing -> {
					existing.syncFromNaver(resolveNickname(profile), profile.profile_image());
					return existing;
				})
				.orElseGet(() -> userRepository.save(
						User.createFromNaver(profile.id(), resolveNickname(profile), profile.profile_image())));

		return issueTokens(user);
	}

	public IssuedTokens refresh(String refreshToken) {
		if (!jwtUtil.isValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
		long userId = jwtUtil.getUserId(refreshToken);
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		return issueTokens(user);
	}

	private IssuedTokens issueTokens(User user) {
		String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());
		String newRefresh = jwtUtil.generateRefreshToken(user.getId());
		return new IssuedTokens(accessToken, newRefresh);
	}

	private String resolveNickname(NaverUserInfoResponse.Profile profile) {
		if (profile.nickname() != null && !profile.nickname().isBlank()) {
			return profile.nickname();
		}
		if (profile.name() != null && !profile.name().isBlank()) {
			return profile.name();
		}
		throw new CustomException(ErrorCode.OAUTH_FAILED);
	}

	public record IssuedTokens(String accessToken, String refreshToken) {
	}
}
