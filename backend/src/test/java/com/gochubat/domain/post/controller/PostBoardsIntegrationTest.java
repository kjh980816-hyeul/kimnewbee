package com.gochubat.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.post.entity.BoardType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostBoardsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User user;
	private String token;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-board", "작성자", Tier.PEPPER));
		token = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());
	}

	@AfterEach
	void cleanup() {
		postRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void free_list_excludes_other_board_types() throws Exception {
		postRepository.save(Post.createFree("자유1", "내용", user));
		postRepository.save(Post.createFanart("팬아트1", "설명", user, "https://img"));
		postRepository.save(Post.createClip("클립1", "설명", user, "https://youtu.be/x"));

		mockMvc.perform(get("/api/free"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].title").value("자유1"));

		mockMvc.perform(get("/api/fanart"))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].thumbnailUrl").value("https://img"));

		mockMvc.perform(get("/api/clips"))
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].source").value("youtube"));
	}

	@Test
	void detail_increments_view_count_within_board() throws Exception {
		Post free = postRepository.save(Post.createFree("자유 디테일", "본문", user));

		mockMvc.perform(get("/api/free/" + free.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.viewCount").value(1))
				.andExpect(jsonPath("$.content").value("본문"))
				.andExpect(jsonPath("$.likedByMe").value(false));

		mockMvc.perform(get("/api/free/" + free.getId()))
				.andExpect(jsonPath("$.viewCount").value(2));
	}

	@Test
	void detail_returns_404_when_id_belongs_to_other_board() throws Exception {
		Post fanart = postRepository.save(Post.createFanart("팬아트", "설명", user, "https://img"));

		mockMvc.perform(get("/api/free/" + fanart.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	void create_free_requires_auth() throws Exception {
		mockMvc.perform(post("/api/free")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("title", "t", "content", "c"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_free_succeeds_with_token_and_persists() throws Exception {
		mockMvc.perform(post("/api/free")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("title", "새 글", "content", "본문"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("새 글"))
				.andExpect(jsonPath("$.author").value("작성자"));

		assertThat(postRepository.count()).isEqualTo(1L);
	}

	@Test
	void create_free_validates_required_fields() throws Exception {
		mockMvc.perform(post("/api/free")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("title", "", "content", "c"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void create_fanart_persists_with_image_url() throws Exception {
		mockMvc.perform(post("/api/fanart")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "팬아트", "imageUrl", "https://img/1.png", "content", "설명"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.imageUrl").value("https://img/1.png"))
				.andExpect(jsonPath("$.thumbnailUrl").value("https://img/1.png"));
	}

	@Test
	void create_clip_detects_youtube_source() throws Exception {
		mockMvc.perform(post("/api/clips")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "클립", "videoUrl", "https://www.youtube.com/watch?v=abc", "description", "설명"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.videoUrl").value("https://www.youtube.com/watch?v=abc"))
				.andExpect(jsonPath("$.source").value("youtube"));
	}

	@Test
	void create_clip_detects_chzzk_source() throws Exception {
		mockMvc.perform(post("/api/clips")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "치지직 클립", "videoUrl", "https://chzzk.naver.com/clips/abc", "description", "설명"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.source").value("chzzk"));
	}
}
