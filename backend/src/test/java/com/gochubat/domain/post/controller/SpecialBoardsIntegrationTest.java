package com.gochubat.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.repository.PostRepository;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SpecialBoardsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User pepper;
	private User corn;
	private String pepperToken;
	private String cornToken;

	@BeforeEach
	void setUp() {
		pepper = userRepository.save(TestUserFactory.create("nv-sp-p", "고추유저", Tier.PEPPER));
		corn = userRepository.save(TestUserFactory.create("nv-sp-c", "옥수수유저", Tier.CORN));
		pepperToken = jwtUtil.generateAccessToken(pepper.getId(), pepper.getTier().name());
		cornToken = jwtUtil.generateAccessToken(corn.getId(), corn.getTier().name());
	}

	@AfterEach
	void cleanup() {
		postLikeRepository.deleteAll();
		postRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void pet_list_is_public() throws Exception {
		postRepository.save(Post.createPet("우리집 강아지", "귀여워요", pepper, "https://img/dog.png"));

		mockMvc.perform(get("/api/pets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].thumbnailUrl").value("https://img/dog.png"));
	}

	@Test
	void pet_create_returns_unauthorized_without_token() throws Exception {
		mockMvc.perform(post("/api/pets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "고양이", "imageUrl", "https://img/cat.png", "content", "야옹"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void pet_create_succeeds_for_authenticated_user_and_persists() throws Exception {
		mockMvc.perform(post("/api/pets")
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "고양이", "imageUrl", "https://img/cat.png", "content", "야옹야옹"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("고양이"))
				.andExpect(jsonPath("$.imageUrl").value("https://img/cat.png"));

		assertThat(postRepository.count()).isEqualTo(1L);
	}

	@Test
	void pet_like_toggle_works_for_authenticated_user() throws Exception {
		Post pet = postRepository.save(Post.createPet("강아지", "귀여움", pepper, "https://img"));

		mockMvc.perform(post("/api/pets/" + pet.getId() + "/like")
						.cookie(new Cookie("access_token", pepperToken)))
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));
	}

	@Test
	void offline_list_returns_403_for_pepper_user() throws Exception {
		mockMvc.perform(get("/api/offline").cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void offline_list_returns_401_for_anonymous() throws Exception {
		mockMvc.perform(get("/api/offline"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void offline_list_succeeds_for_corn_user() throws Exception {
		postRepository.save(Post.createOffline(
				"부산 모임", "재밌었어요" + "x".repeat(80), corn,
				"https://img/busan.png", "부산", LocalDate.of(2026, 4, 1)
		));

		mockMvc.perform(get("/api/offline").cookie(new Cookie("access_token", cornToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].location").value("부산"))
				.andExpect(jsonPath("$.data[0].meetupDate").value("2026-04-01"))
				.andExpect(jsonPath("$.data[0].preview").exists());
	}

	@Test
	void offline_create_succeeds_for_corn_user_and_validates_location_meetup() throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("title", "서울 모임");
		body.put("location", "서울");
		body.put("meetupDate", "2026-05-15");
		body.put("imageUrl", "https://img/seoul.png");
		body.put("content", "기대돼요");

		mockMvc.perform(post("/api/offline")
						.cookie(new Cookie("access_token", cornToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.location").value("서울"))
				.andExpect(jsonPath("$.meetupDate").value("2026-05-15"));
	}

	@Test
	void offline_detail_blocked_for_pepper() throws Exception {
		Post offline = postRepository.save(Post.createOffline(
				"제주 모임", "본문", corn, "https://img", "제주", LocalDate.of(2026, 6, 1)));

		mockMvc.perform(get("/api/offline/" + offline.getId())
						.cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isForbidden());
	}

	@Test
	void offline_like_blocked_for_pepper_role() throws Exception {
		Post offline = postRepository.save(Post.createOffline(
				"속초 모임", "본문", corn, "https://img", "속초", LocalDate.of(2026, 7, 1)));

		mockMvc.perform(post("/api/offline/" + offline.getId() + "/like")
						.cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isForbidden());
	}

	@Test
	void offline_like_succeeds_for_corn() throws Exception {
		Post offline = postRepository.save(Post.createOffline(
				"여수 모임", "본문", corn, "https://img", "여수", LocalDate.of(2026, 8, 1)));

		mockMvc.perform(post("/api/offline/" + offline.getId() + "/like")
						.cookie(new Cookie("access_token", cornToken)))
				.andExpect(jsonPath("$.liked").value(true));
	}
}
