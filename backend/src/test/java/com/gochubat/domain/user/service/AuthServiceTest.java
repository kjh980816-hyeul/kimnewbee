package com.gochubat.domain.user.service;

import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import com.gochubat.global.jwt.JwtProperties;
import com.gochubat.global.jwt.JwtUtil;
import com.gochubat.global.oauth.NaverOauthClient;
import com.gochubat.global.oauth.NaverTokenResponse;
import com.gochubat.global.oauth.NaverUserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

class AuthServiceTest {

	private static final String SECRET = "test-secret-DO-NOT-USE-IN-PRODUCTION-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa==";

	private NaverOauthClient naverOauthClient;
	private UserRepository userRepository;
	private JwtUtil jwtUtil;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		naverOauthClient = Mockito.mock(NaverOauthClient.class);
		userRepository = Mockito.mock(UserRepository.class);
		jwtUtil = new JwtUtil(new JwtProperties(SECRET, 60_000L, 600_000L));
		authService = new AuthService(naverOauthClient, userRepository, jwtUtil);
	}

	@Test
	void login_creates_user_when_naver_id_unknown() {
		stubNaver("nv-100", "준하", null);
		Mockito.when(userRepository.findByNaverId("nv-100")).thenReturn(Optional.empty());
		Mockito.when(userRepository.save(any(User.class))).thenAnswer(inv -> {
			User u = inv.getArgument(0);
			setId(u, 1L);
			return u;
		});

		AuthService.IssuedTokens tokens = authService.loginWithNaver("code", "state");

		assertThat(tokens.accessToken()).isNotBlank();
		assertThat(tokens.refreshToken()).isNotBlank();
		assertThat(jwtUtil.getUserId(tokens.accessToken())).isEqualTo(1L);
		assertThat(jwtUtil.getRole(tokens.accessToken())).isEqualTo("SEED");
		assertThat(jwtUtil.isAccessToken(tokens.accessToken())).isTrue();
		assertThat(jwtUtil.isRefreshToken(tokens.refreshToken())).isTrue();

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getNaverId()).isEqualTo("nv-100");
		assertThat(captor.getValue().getNickname()).isEqualTo("준하");
	}

	@Test
	void login_syncs_existing_user_without_resaving() {
		stubNaver("nv-200", "새닉네임", "https://new.png");
		User existing = User.createFromNaver("nv-200", "이전닉네임", null);
		setId(existing, 5L);
		Mockito.when(userRepository.findByNaverId("nv-200")).thenReturn(Optional.of(existing));

		AuthService.IssuedTokens tokens = authService.loginWithNaver("code", "state");

		assertThat(jwtUtil.getUserId(tokens.accessToken())).isEqualTo(5L);
		assertThat(existing.getNickname()).isEqualTo("새닉네임");
		assertThat(existing.getProfileImage()).isEqualTo("https://new.png");
		Mockito.verify(userRepository, Mockito.never()).save(any());
	}

	@Test
	void login_throws_oauth_failed_when_nickname_and_name_both_blank() {
		stubNaver("nv-300", null, null);
		Mockito.when(userRepository.findByNaverId("nv-300")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.loginWithNaver("code", "state"))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.OAUTH_FAILED);
	}

	@Test
	void refresh_issues_new_token_pair_when_user_exists() {
		User user = User.createFromNaver("nv-refresh", "닉", null);
		setId(user, 9L);
		Mockito.when(userRepository.findById(9L)).thenReturn(Optional.of(user));

		String oldRefresh = jwtUtil.generateRefreshToken(9L);
		AuthService.IssuedTokens tokens = authService.refresh(oldRefresh);

		assertThat(jwtUtil.getUserId(tokens.accessToken())).isEqualTo(9L);
		assertThat(jwtUtil.getRole(tokens.accessToken())).isEqualTo("SEED");
		assertThat(jwtUtil.isAccessToken(tokens.accessToken())).isTrue();
		assertThat(jwtUtil.isRefreshToken(tokens.refreshToken())).isTrue();
	}

	@Test
	void refresh_rejects_access_token() {
		String accessToken = jwtUtil.generateAccessToken(9L, "SEED");

		assertThatThrownBy(() -> authService.refresh(accessToken))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
		Mockito.verify(userRepository, Mockito.never()).findById(any());
	}

	@Test
	void refresh_rejects_invalid_token() {
		assertThatThrownBy(() -> authService.refresh("garbage.token.value"))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	@Test
	void refresh_rejects_when_user_deleted() {
		Mockito.when(userRepository.findById(77L)).thenReturn(Optional.empty());
		String refresh = jwtUtil.generateRefreshToken(77L);

		assertThatThrownBy(() -> authService.refresh(refresh))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	private void stubNaver(String naverId, String nickname, String profileImage) {
		Mockito.when(naverOauthClient.exchangeCodeForToken("code", "state"))
				.thenReturn(new NaverTokenResponse("access-xxx"));
		NaverUserInfoResponse.Profile profile = new NaverUserInfoResponse.Profile(naverId, nickname, null, profileImage);
		Mockito.when(naverOauthClient.fetchUserInfo("access-xxx"))
				.thenReturn(new NaverUserInfoResponse("00", "success", profile));
	}

	private static void setId(User user, long id) {
		try {
			Field idField = User.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(user, id);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
