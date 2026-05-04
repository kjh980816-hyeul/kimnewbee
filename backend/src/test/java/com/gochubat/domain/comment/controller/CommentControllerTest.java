package com.gochubat.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.comment.entity.Comment;
import com.gochubat.domain.comment.repository.CommentRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User user;
	private User other;
	private Post post;
	private String userToken;
	private String otherToken;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-cmt", "댓글러", Tier.PEPPER));
		other = userRepository.save(TestUserFactory.create("nv-cmt-2", "타인", Tier.PEPPER));
		post = postRepository.save(Post.createFree("자유 글", "본문", user));
		userToken = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());
		otherToken = jwtUtil.generateAccessToken(other.getId(), other.getTier().name());
	}

	@AfterEach
	void cleanup() {
		commentRepository.deleteAll();
		postRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void list_is_public_when_post_exists() throws Exception {
		commentRepository.save(Comment.create(post.getId(), null, user, "첫댓"));

		mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].content").value("첫댓"))
				.andExpect(jsonPath("$[0].author").value("댓글러"))
				.andExpect(jsonPath("$[0].deleted").value(false));
	}

	@Test
	void list_returns_404_for_unknown_post() throws Exception {
		mockMvc.perform(get("/api/posts/99999/comments"))
				.andExpect(status().isNotFound());
	}

	@Test
	void create_requires_auth() throws Exception {
		mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("content", "댓글"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_root_comment_persists() throws Exception {
		Map<String, Object> body = new HashMap<>();
		body.put("parentId", null);
		body.put("content", "안녕");

		mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
						.cookie(new Cookie("access_token", userToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.content").value("안녕"))
				.andExpect(jsonPath("$.parentId").doesNotExist())
				.andExpect(jsonPath("$.postId").value(post.getId()));

		assertThat(commentRepository.count()).isEqualTo(1L);
	}

	@Test
	void create_reply_validates_parent_belongs_to_post() throws Exception {
		Post otherPost = postRepository.save(Post.createFree("다른 글", "x", user));
		Comment otherParent = commentRepository.save(Comment.create(otherPost.getId(), null, user, "다른 댓글"));

		Map<String, Object> body = new HashMap<>();
		body.put("parentId", otherParent.getId());
		body.put("content", "잘못된 답글");

		mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
						.cookie(new Cookie("access_token", userToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void create_validates_blank_content() throws Exception {
		mockMvc.perform(post("/api/posts/" + post.getId() + "/comments")
						.cookie(new Cookie("access_token", userToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Map.of("content", ""))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void delete_returns_forbidden_when_not_owner() throws Exception {
		Comment comment = commentRepository.save(Comment.create(post.getId(), null, user, "내댓글"));

		mockMvc.perform(delete("/api/comments/" + comment.getId())
						.cookie(new Cookie("access_token", otherToken)))
				.andExpect(status().isForbidden());

		assertThat(commentRepository.findById(comment.getId()).orElseThrow().isDeleted()).isFalse();
	}

	@Test
	void delete_soft_deletes_for_owner() throws Exception {
		Comment comment = commentRepository.save(Comment.create(post.getId(), null, user, "삭제할 댓글"));

		mockMvc.perform(delete("/api/comments/" + comment.getId())
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(status().isNoContent());

		Comment reloaded = commentRepository.findById(comment.getId()).orElseThrow();
		assertThat(reloaded.isDeleted()).isTrue();

		mockMvc.perform(get("/api/posts/" + post.getId() + "/comments"))
				.andExpect(jsonPath("$[0].deleted").value(true))
				.andExpect(jsonPath("$[0].content").value("삭제된 댓글이에요"));
	}

	@Test
	void post_detail_reflects_active_comment_count() throws Exception {
		commentRepository.save(Comment.create(post.getId(), null, user, "1"));
		Comment c2 = commentRepository.save(Comment.create(post.getId(), null, user, "2"));
		commentRepository.save(Comment.create(post.getId(), null, user, "3"));
		c2.softDelete();
		commentRepository.save(c2);

		mockMvc.perform(get("/api/free/" + post.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.commentCount").value(2));
	}

	@Test
	void post_list_reflects_active_comment_count_per_post() throws Exception {
		Post second = postRepository.save(Post.createFree("두번째", "본문", user));
		commentRepository.save(Comment.create(post.getId(), null, user, "a"));
		commentRepository.save(Comment.create(post.getId(), null, user, "b"));
		commentRepository.save(Comment.create(second.getId(), null, user, "c"));

		mockMvc.perform(get("/api/free"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[?(@.id == " + post.getId() + ")].commentCount").value(2))
				.andExpect(jsonPath("$.data[?(@.id == " + second.getId() + ")].commentCount").value(1));
	}
}
