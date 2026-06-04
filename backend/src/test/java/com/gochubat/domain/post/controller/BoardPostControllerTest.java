package com.gochubat.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.board.entity.Board;
import com.gochubat.domain.board.entity.BoardLayout;
import com.gochubat.domain.board.repository.BoardRepository;
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
class BoardPostControllerTest {

	private static final String SLUG = "test-custom-board";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private Board board;
	private User pepper;
	private User seed;
	private String pepperToken;
	private String seedToken;

	@BeforeEach
	void setUp() {
		// 쓰기 권한 PEPPER 이상, 읽기 SEED인 커스텀 게시판
		board = boardRepository.save(Board.create(SLUG, "테스트 게시판", BoardLayout.LIST, Tier.SEED, Tier.PEPPER, 99));
		pepper = userRepository.save(TestUserFactory.create("nv-bp-pepper", "고추", Tier.PEPPER));
		seed = userRepository.save(TestUserFactory.create("nv-bp-seed", "새싹", Tier.SEED));
		pepperToken = jwtUtil.generateAccessToken(pepper.getId(), pepper.getTier().name());
		seedToken = jwtUtil.generateAccessToken(seed.getId(), seed.getTier().name());
	}

	@AfterEach
	void cleanup() {
		// 시드된 기본 게시판은 건드리지 않고, 이 테스트가 만든 글/보드/유저만 정리
		postRepository.findByBoardSlugWithAuthor(SLUG).forEach(p -> postRepository.deleteById(p.getId()));
		boardRepository.delete(board);
		userRepository.deleteAll();
	}

	@Test
	void pepper_creates_custom_post() throws Exception {
		mockMvc.perform(post("/api/board-posts/" + SLUG)
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "첫 글", "content", "커스텀 게시판 본문"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("첫 글"))
				.andExpect(jsonPath("$.author").value("고추"))
				.andExpect(jsonPath("$.authorTier").value("pepper"));

		assertThat(postRepository.findByBoardSlugWithAuthor(SLUG)).hasSize(1);
	}

	@Test
	void seed_cannot_create_when_below_write_tier() throws Exception {
		mockMvc.perform(post("/api/board-posts/" + SLUG)
						.cookie(new Cookie("access_token", seedToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "막혀야 함", "content", "권한 미달"))))
				.andExpect(status().isForbidden());

		assertThat(postRepository.findByBoardSlugWithAuthor(SLUG)).isEmpty();
	}

	@Test
	void create_requires_auth() throws Exception {
		mockMvc.perform(post("/api/board-posts/" + SLUG)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "t", "content", "c"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_returns_404_for_unknown_board() throws Exception {
		mockMvc.perform(post("/api/board-posts/no-such-board")
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "t", "content", "c"))))
				.andExpect(status().isNotFound());
	}

	@Test
	void list_is_public_and_returns_posts() throws Exception {
		postRepository.save(Post.createCustom(SLUG, "공개 글", "내용", pepper, null));

		mockMvc.perform(get("/api/board-posts/" + SLUG))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].title").value("공개 글"))
				.andExpect(jsonPath("$.data[0].author").value("고추"));
	}

	@Test
	void detail_is_public_and_exposes_author_tier() throws Exception {
		Post saved = postRepository.save(Post.createCustom(SLUG, "상세 글", "본문", pepper, null));

		mockMvc.perform(get("/api/board-posts/" + SLUG + "/" + saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("상세 글"))
				.andExpect(jsonPath("$.content").value("본문"))
				.andExpect(jsonPath("$.authorTier").value("pepper"));
	}

	@Test
	void list_returns_404_for_unknown_board() throws Exception {
		mockMvc.perform(get("/api/board-posts/no-such-board"))
				.andExpect(status().isNotFound());
	}
}
