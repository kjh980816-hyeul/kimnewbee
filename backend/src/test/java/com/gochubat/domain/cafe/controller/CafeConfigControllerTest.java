package com.gochubat.domain.cafe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.cafe.repository.CafeConfigRepository;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.jwt.JwtUtil;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CafeConfigControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CafeConfigRepository repository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User pepper;
	private User owner;
	private String pepperToken;
	private String ownerToken;

	@BeforeEach
	void setUp() {
		pepper = userRepository.save(TestUserFactory.create("nv-cf-p", "고추", Tier.PEPPER));
		owner = userRepository.save(TestUserFactory.create("nv-cf-o", "주인", Tier.OWNER));
		pepperToken = jwtUtil.generateAccessToken(pepper.getId(), pepper.getTier().name());
		ownerToken = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());
	}

	@AfterEach
	void cleanup() {
		repository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void public_get_returns_default_when_unset() throws Exception {
		mockMvc.perform(get("/api/cafe/config"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.heroHeadline").exists())
				.andExpect(jsonPath("$.footerText").exists());
	}

	@Test
	void admin_update_403_for_pepper() throws Exception {
		Map<String, Object> body = Map.of(
				"heroHeadline", "수정",
				"heroBannerUrl", "https://x/y.png",
				"heroSubtext", "서브",
				"footerText", "푸터"
		);
		mockMvc.perform(put("/api/admin/cafe/config")
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isForbidden());
	}

	@Test
	void admin_update_persists_for_owner_and_public_get_reflects() throws Exception {
		Map<String, Object> body = Map.of(
				"heroHeadline", "새 헤드라인",
				"heroBannerUrl", "https://img/banner.png",
				"heroSubtext", "새 서브",
				"footerText", "새 푸터"
		);
		mockMvc.perform(put("/api/admin/cafe/config")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.heroHeadline").value("새 헤드라인"))
				.andExpect(jsonPath("$.heroBannerUrl").value("https://img/banner.png"));

		mockMvc.perform(get("/api/cafe/config"))
				.andExpect(jsonPath("$.heroHeadline").value("새 헤드라인"))
				.andExpect(jsonPath("$.footerText").value("새 푸터"));
	}

	@Test
	void admin_update_validates_required_headline() throws Exception {
		Map<String, Object> body = new java.util.HashMap<>();
		body.put("heroHeadline", "");
		body.put("heroBannerUrl", null);
		body.put("heroSubtext", null);
		body.put("footerText", null);
		mockMvc.perform(put("/api/admin/cafe/config")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}
}
