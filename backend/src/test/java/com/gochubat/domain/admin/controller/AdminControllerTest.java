package com.gochubat.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.comment.entity.Comment;
import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.like.entity.PostLike;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User pepper;
	private User owner;
	private String pepperToken;
	private String ownerToken;

	@BeforeEach
	void setUp() {
		pepper = userRepository.save(TestUserFactory.create("nv-adm-p", "고추", Tier.PEPPER));
		owner = userRepository.save(TestUserFactory.create("nv-adm-o", "주인", Tier.OWNER));
		pepperToken = jwtUtil.generateAccessToken(pepper.getId(), pepper.getTier().name());
		ownerToken = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());
	}

	@AfterEach
	void cleanup() {
		commentRepository.deleteAll();
		postLikeRepository.deleteAll();
		postRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void dashboard_requires_owner_returns_401_for_anonymous() throws Exception {
		mockMvc.perform(get("/api/admin/dashboard"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void dashboard_returns_403_for_pepper() throws Exception {
		mockMvc.perform(get("/api/admin/dashboard").cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void dashboard_returns_aggregated_counts_for_owner() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", pepper));
		commentRepository.save(Comment.create(post.getId(), null, pepper, "댓글"));
		postLikeRepository.save(PostLike.create(post.getId(), owner.getId()));

		mockMvc.perform(get("/api/admin/dashboard").cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalUsers").value(2))
				.andExpect(jsonPath("$.totalPosts").value(1))
				.andExpect(jsonPath("$.totalComments").value(1))
				.andExpect(jsonPath("$.totalLikes").value(1));
	}

	@Test
	void users_returns_list_for_owner_only() throws Exception {
		mockMvc.perform(get("/api/admin/users").cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/admin/users").cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(2))
				.andExpect(jsonPath("$.data[?(@.nickname == '주인')].tier").value("owner"))
				.andExpect(jsonPath("$.data[?(@.nickname == '고추')].tier").value("pepper"));
	}

	@Test
	void change_tier_promotes_pepper_to_corn() throws Exception {
		mockMvc.perform(patch("/api/admin/users/" + pepper.getId() + "/tier")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("tier", "CORN"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tier").value("corn"));

		assertThat(userRepository.findById(pepper.getId()).orElseThrow().getTier()).isEqualTo(Tier.CORN);
	}

	@Test
	void change_tier_can_demote() throws Exception {
		mockMvc.perform(patch("/api/admin/users/" + pepper.getId() + "/tier")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("tier", "SEED"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tier").value("seed"));
	}

	@Test
	void change_tier_returns_404_for_unknown_user() throws Exception {
		mockMvc.perform(patch("/api/admin/users/99999/tier")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("tier", "PEPPER"))))
				.andExpect(status().isNotFound());
	}

	@Test
	void change_tier_returns_403_for_non_owner() throws Exception {
		mockMvc.perform(patch("/api/admin/users/" + pepper.getId() + "/tier")
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("tier", "OWNER"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void adjust_points_adds_delta() throws Exception {
		mockMvc.perform(patch("/api/admin/users/" + pepper.getId() + "/points")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("delta", 50))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.points").value(50));
	}

	@Test
	void adjust_points_floor_zero_on_negative_overflow() throws Exception {
		mockMvc.perform(patch("/api/admin/users/" + pepper.getId() + "/points")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("delta", -100))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.points").value(0));
	}
}
