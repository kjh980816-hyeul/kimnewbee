package com.gochubat.domain.post.service;

import com.gochubat.domain.point.PointService;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostServiceTest {

	private PostRepository postRepository;
	private UserRepository userRepository;
	private PointService pointService;
	private PostService postService;
	private User author;

	@BeforeEach
	void setUp() {
		postRepository = Mockito.mock(PostRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		pointService = Mockito.mock(PointService.class);
		postService = new PostService(postRepository, userRepository, pointService);
		author = TestUserFactory.create("nv-a", "작성자", Tier.PEPPER);
		TestUserFactory.setId(author, 1L);
	}

	@Test
	void list_delegates_to_repository_with_type() {
		Post post = Post.createFree("a", "b", author);
		Mockito.when(postRepository.findByTypeWithAuthor(BoardType.FREE)).thenReturn(List.of(post));

		List<Post> result = postService.list(BoardType.FREE);

		assertThat(result).containsExactly(post);
	}

	@Test
	void view_detail_increments_view_count_and_returns_post() {
		Post post = Post.createFree("t", "c", author);
		Mockito.when(postRepository.findByIdAndTypeWithAuthor(7L, BoardType.FREE)).thenReturn(Optional.of(post));

		Post result = postService.viewDetail(BoardType.FREE, 7L);

		assertThat(result.getViewCount()).isEqualTo(1L);
		assertThat(post.getViewCount()).isEqualTo(1L);
	}

	@Test
	void view_detail_throws_not_found_when_missing() {
		Mockito.when(postRepository.findByIdAndTypeWithAuthor(99L, BoardType.FREE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> postService.viewDetail(BoardType.FREE, 99L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	void load_author_throws_unauthorized_when_missing() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> postService.loadAuthor(99L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}
}
