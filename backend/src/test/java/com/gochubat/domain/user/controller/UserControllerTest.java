package com.gochubat.domain.user.controller;

import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.jwt.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@AfterEach
	void cleanup() {
		userRepository.deleteAll();
	}

	@Test
	void me_returns_unauthorized_without_token() throws Exception {
		mockMvc.perform(get("/api/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void me_returns_current_user_with_valid_cookie_token() throws Exception {
		User user = userRepository.save(User.createFromNaver("nv-it-1", "통합테스트", null));
		String token = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());

		mockMvc.perform(get("/api/me").cookie(new Cookie("access_token", token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickname").value("통합테스트"))
				.andExpect(jsonPath("$.tier").value("seed"))
				.andExpect(jsonPath("$.points").value(0));
	}

	@Test
	void me_returns_current_user_with_valid_authorization_header() throws Exception {
		User user = userRepository.save(User.createFromNaver("nv-it-h", "헤더테스트", null));
		String token = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());

		mockMvc.perform(get("/api/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nickname").value("헤더테스트"));
	}

	@Test
	void me_returns_unauthorized_when_token_user_missing() throws Exception {
		String token = jwtUtil.generateAccessToken(99999L, "SEED");

		mockMvc.perform(get("/api/me").cookie(new Cookie("access_token", token)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void me_rejects_refresh_token() throws Exception {
		User user = userRepository.save(User.createFromNaver("nv-it-r", "리프레시테스트", null));
		String refreshToken = jwtUtil.generateRefreshToken(user.getId());

		mockMvc.perform(get("/api/me").cookie(new Cookie("access_token", refreshToken)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void stats_returns_zero_for_authenticated_user() throws Exception {
		User user = userRepository.save(User.createFromNaver("nv-it-2", "통계테스트", null));
		String token = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());

		mockMvc.perform(get("/api/me/stats").cookie(new Cookie("access_token", token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postCount").value(0))
				.andExpect(jsonPath("$.commentCount").value(0))
				.andExpect(jsonPath("$.likeGivenCount").value(0))
				.andExpect(jsonPath("$.attendanceStreak").value(0));
	}
}
