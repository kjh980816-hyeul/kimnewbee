package com.gochubat.domain.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.comment.repository.CommentRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointAwardIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User author;
	private User other;
	private String authorToken;
	private String otherToken;

	@BeforeEach
	void setUp() {
		author = userRepository.save(TestUserFactory.create("nv-pt-a", "작성자", Tier.SEED));
		other = userRepository.save(TestUserFactory.create("nv-pt-o", "타인", Tier.SEED));
		authorToken = jwtUtil.generateAccessToken(author.getId(), author.getTier().name());
		otherToken = jwtUtil.generateAccessToken(other.getId(), other.getTier().name());
	}

	@AfterEach
	void cleanup() {
		commentRepository.deleteAll();
		postLikeRepository.deleteAll();
		postRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void post_creation_awards_10_points() throws Exception {
		mockMvc.perform(post("/api/free")
						.cookie(new Cookie("access_token", authorToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("title", "t", "content", "c"))))
				.andExpect(status().isCreated());

		User reloaded = userRepository.findById(author.getId()).orElseThrow();
		assertThat(reloaded.getPoints()).isEqualTo(10L);
	}

	@Test
	void comment_creation_awards_2_points_to_commenter() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", author));

		mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
						.cookie(new Cookie("access_token", otherToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("content", "댓글"))))
				.andExpect(status().isCreated());

		User reloaded = userRepository.findById(other.getId()).orElseThrow();
		assertThat(reloaded.getPoints()).isEqualTo(2L);
	}

	@Test
	void like_received_awards_to_post_author_revoke_subtracts() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", author));

		mockMvc.perform(post("/api/free/" + post.getId() + "/like")
						.cookie(new Cookie("access_token", otherToken)))
				.andExpect(jsonPath("$.liked").value(true));

		assertThat(userRepository.findById(author.getId()).orElseThrow().getPoints()).isEqualTo(1L);

		mockMvc.perform(post("/api/free/" + post.getId() + "/like")
						.cookie(new Cookie("access_token", otherToken)))
				.andExpect(jsonPath("$.liked").value(false));

		assertThat(userRepository.findById(author.getId()).orElseThrow().getPoints()).isZero();
	}

	@Test
	void points_reach_pepper_threshold_promotes_tier_automatically() throws Exception {
		for (int i = 0; i < 10; i++) {
			mockMvc.perform(post("/api/free")
							.cookie(new Cookie("access_token", authorToken))
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(Map.of("title", "t" + i, "content", "c"))))
					.andExpect(status().isCreated());
		}

		User reloaded = userRepository.findById(author.getId()).orElseThrow();
		assertThat(reloaded.getPoints()).isEqualTo(100L);
		assertThat(reloaded.getTier()).isEqualTo(Tier.PEPPER);
	}

	@Test
	void me_stats_reflects_real_counts() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", author));
		mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
						.cookie(new Cookie("access_token", authorToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("content", "내 댓글"))));
		mockMvc.perform(post("/api/free/" + post.getId() + "/like")
						.cookie(new Cookie("access_token", authorToken)));

		mockMvc.perform(get("/api/me/stats").cookie(new Cookie("access_token", authorToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.postCount").value(1))
				.andExpect(jsonPath("$.commentCount").value(1))
				.andExpect(jsonPath("$.likeGivenCount").value(1))
				.andExpect(jsonPath("$.attendanceStreak").value(0));
	}
}
