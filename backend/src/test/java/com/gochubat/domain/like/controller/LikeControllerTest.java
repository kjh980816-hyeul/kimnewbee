package com.gochubat.domain.like.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LikeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	private User user;
	private User other;
	private String userToken;
	private String otherToken;
	private Post freePost;
	private Post fanartPost;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-l1", "사용자1", Tier.PEPPER));
		other = userRepository.save(TestUserFactory.create("nv-l2", "사용자2", Tier.PEPPER));
		userToken = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());
		otherToken = jwtUtil.generateAccessToken(other.getId(), other.getTier().name());
		freePost = postRepository.save(Post.createFree("자유 글", "본문", user));
		fanartPost = postRepository.save(Post.createFanart("팬아트 글", "설명", user, "https://img"));
	}

	@AfterEach
	void cleanup() {
		postLikeRepository.deleteAll();
		postRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void toggle_requires_authentication() throws Exception {
		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void toggle_returns_404_when_board_type_mismatch() throws Exception {
		mockMvc.perform(post("/api/free/" + fanartPost.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void toggle_adds_then_removes_like_round_trip() throws Exception {
		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));

		assertThat(postLikeRepository.findByPostIdAndUserId(freePost.getId(), user.getId())).isPresent();

		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0));

		assertThat(postLikeRepository.findByPostIdAndUserId(freePost.getId(), user.getId())).isEmpty();
	}

	@Test
	void toggle_aggregates_likes_across_users_in_post_count() throws Exception {
		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)));
		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like")
						.cookie(new Cookie("access_token", otherToken)));

		mockMvc.perform(get("/api/free/" + freePost.getId())
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(jsonPath("$.likeCount").value(2))
				.andExpect(jsonPath("$.likedByMe").value(true));

		mockMvc.perform(get("/api/free/" + freePost.getId())
						.cookie(new Cookie("access_token", otherToken)))
				.andExpect(jsonPath("$.likedByMe").value(true));
	}

	@Test
	void detail_likedByMe_false_for_anonymous() throws Exception {
		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)));

		mockMvc.perform(get("/api/free/" + freePost.getId()))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.likedByMe").value(false));
	}

	@Test
	void list_aggregates_like_counts_per_post() throws Exception {
		Post second = postRepository.save(Post.createFree("두번째", "본문", user));

		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)));
		mockMvc.perform(post("/api/free/" + freePost.getId() + "/like")
						.cookie(new Cookie("access_token", otherToken)));
		mockMvc.perform(post("/api/free/" + second.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)));

		mockMvc.perform(get("/api/free"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.id == " + freePost.getId() + ")].likeCount").value(2))
				.andExpect(jsonPath("$.data[?(@.id == " + second.getId() + ")].likeCount").value(1));
	}

	@Test
	void toggle_works_for_fanart_and_clip_boards() throws Exception {
		mockMvc.perform(post("/api/fanart/" + fanartPost.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1));

		Post clip = postRepository.save(Post.createClip("클립", "설명", user, "https://youtu.be/x"));
		mockMvc.perform(post("/api/clips/" + clip.getId() + "/like")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(jsonPath("$.liked").value(true));
	}
}
