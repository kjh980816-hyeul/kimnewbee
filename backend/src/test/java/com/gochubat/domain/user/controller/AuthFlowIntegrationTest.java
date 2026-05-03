package com.gochubat.domain.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void naver_login_redirects_to_naver_with_state_cookie() throws Exception {
		mockMvc.perform(get("/api/auth/naver/login"))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("nid.naver.com")))
				.andExpect(cookie().exists("oauth_state"))
				.andExpect(cookie().httpOnly("oauth_state", true));
	}

	@Test
	void callback_rejects_when_state_cookie_missing() throws Exception {
		mockMvc.perform(get("/api/auth/naver/callback")
						.param("code", "any-code")
						.param("state", "anything"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("OAUTH_STATE_MISMATCH"));
	}

	@Test
	void callback_rejects_when_state_mismatch() throws Exception {
		mockMvc.perform(get("/api/auth/naver/callback")
						.param("code", "any-code")
						.param("state", "incoming-state")
						.cookie(new Cookie("oauth_state", "different-state")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("OAUTH_STATE_MISMATCH"));
	}

	@Test
	void refresh_returns_unauthorized_when_no_cookie() throws Exception {
		mockMvc.perform(post("/api/auth/refresh"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void logout_clears_auth_cookies() throws Exception {
		var result = mockMvc.perform(post("/api/auth/logout"))
				.andExpect(status().isNoContent())
				.andReturn();

		Cookie access = result.getResponse().getCookie("access_token");
		Cookie refresh = result.getResponse().getCookie("refresh_token");
		assertThat(access).isNotNull();
		assertThat(access.getMaxAge()).isZero();
		assertThat(refresh).isNotNull();
		assertThat(refresh.getMaxAge()).isZero();
	}
}
