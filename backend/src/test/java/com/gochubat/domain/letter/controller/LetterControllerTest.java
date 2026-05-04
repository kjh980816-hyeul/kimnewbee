package com.gochubat.domain.letter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.letter.entity.Letter;
import com.gochubat.domain.letter.repository.LetterRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LetterControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private LetterRepository letterRepository;

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
		pepper = userRepository.save(TestUserFactory.create("nv-letter-p", "팬", Tier.PEPPER));
		owner = userRepository.save(TestUserFactory.create("nv-letter-o", "주인", Tier.OWNER));
		pepperToken = jwtUtil.generateAccessToken(pepper.getId(), pepper.getTier().name());
		ownerToken = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());
	}

	@AfterEach
	void cleanup() {
		letterRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void list_is_public_and_returns_only_preview_fields() throws Exception {
		letterRepository.save(Letter.create(pepper, "x".repeat(50)));

		mockMvc.perform(get("/api/letters"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].author").value("팬"))
				.andExpect(jsonPath("$.data[0].preview").value("x".repeat(30) + "..."))
				.andExpect(jsonPath("$.data[0].content").doesNotExist());
	}

	@Test
	void create_requires_auth() throws Exception {
		mockMvc.perform(post("/api/letters")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("content", "내 편지"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_succeeds_for_authenticated_pepper_user() throws Exception {
		mockMvc.perform(post("/api/letters")
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("content", "오늘도 응원해요"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.content").value("오늘도 응원해요"))
				.andExpect(jsonPath("$.isReadByAdmin").value(false));

		assertThat(letterRepository.count()).isEqualTo(1L);
	}

	@Test
	void create_validates_blank_content() throws Exception {
		mockMvc.perform(post("/api/letters")
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("content", ""))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void detail_requires_owner_role_returns_403_for_pepper() throws Exception {
		Letter letter = letterRepository.save(Letter.create(pepper, "비밀 편지"));

		mockMvc.perform(get("/api/letters/" + letter.getId())
						.cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void detail_returns_401_when_no_token() throws Exception {
		Letter letter = letterRepository.save(Letter.create(pepper, "비밀 편지"));

		mockMvc.perform(get("/api/letters/" + letter.getId()))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void detail_returns_full_content_for_owner_and_marks_read() throws Exception {
		Letter letter = letterRepository.save(Letter.create(pepper, "관리자가 읽을 편지"));

		mockMvc.perform(get("/api/letters/" + letter.getId())
						.cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").value("관리자가 읽을 편지"))
				.andExpect(jsonPath("$.isReadByAdmin").value(true));

		assertThat(letterRepository.findById(letter.getId()).orElseThrow().isReadByAdmin()).isTrue();
	}

	@Test
	void detail_returns_404_when_letter_missing() throws Exception {
		mockMvc.perform(get("/api/letters/99999")
						.cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void admin_endpoint_returns_false_for_anonymous() throws Exception {
		mockMvc.perform(get("/api/me/admin"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isAdmin").value(false));
	}

	@Test
	void admin_endpoint_returns_false_for_pepper_user() throws Exception {
		mockMvc.perform(get("/api/me/admin").cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isAdmin").value(false));
	}

	@Test
	void admin_endpoint_returns_true_for_owner() throws Exception {
		mockMvc.perform(get("/api/me/admin").cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.isAdmin").value(true));
	}
}
