package com.gochubat.domain.post.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

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

	private User author;
	private User stranger;
	private User owner;
	private String authorToken;
	private String strangerToken;
	private String ownerToken;

	@BeforeEach
	void setUp() {
		author = userRepository.save(TestUserFactory.create("nv-post-a", "작성자", Tier.PEPPER));
		stranger = userRepository.save(TestUserFactory.create("nv-post-s", "타인", Tier.PEPPER));
		owner = userRepository.save(TestUserFactory.create("nv-post-o", "주인", Tier.OWNER));
		authorToken = jwtUtil.generateAccessToken(author.getId(), author.getTier().name());
		strangerToken = jwtUtil.generateAccessToken(stranger.getId(), stranger.getTier().name());
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
	void author_deletes_own_post_with_comments_and_likes() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", author));
		commentRepository.save(Comment.create(post.getId(), null, stranger, "댓글"));
		postLikeRepository.save(PostLike.create(post.getId(), stranger.getId()));

		mockMvc.perform(delete("/api/posts/" + post.getId())
						.cookie(new Cookie("access_token", authorToken)))
				.andExpect(status().isNoContent());

		assertThat(postRepository.existsById(post.getId())).isFalse();
		assertThat(commentRepository.countActiveByPostId(post.getId())).isZero();
		assertThat(postLikeRepository.countByPostId(post.getId())).isZero();
	}

	@Test
	void owner_deletes_others_post() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", author));

		mockMvc.perform(delete("/api/posts/" + post.getId())
						.cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isNoContent());

		assertThat(postRepository.existsById(post.getId())).isFalse();
	}

	@Test
	void stranger_cannot_delete_others_post() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", author));

		mockMvc.perform(delete("/api/posts/" + post.getId())
						.cookie(new Cookie("access_token", strangerToken)))
				.andExpect(status().isForbidden());

		assertThat(postRepository.existsById(post.getId())).isTrue();
	}

	@Test
	void delete_returns_404_for_unknown_post() throws Exception {
		mockMvc.perform(delete("/api/posts/99999")
						.cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void delete_requires_authentication() throws Exception {
		Post post = postRepository.save(Post.createFree("자유", "본문", author));

		mockMvc.perform(delete("/api/posts/" + post.getId()))
				.andExpect(status().isUnauthorized());

		assertThat(postRepository.existsById(post.getId())).isTrue();
	}
}
