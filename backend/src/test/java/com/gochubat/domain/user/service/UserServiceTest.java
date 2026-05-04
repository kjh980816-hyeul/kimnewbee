package com.gochubat.domain.user.service;

import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

	private UserRepository userRepository;
	private PostRepository postRepository;
	private CommentRepository commentRepository;
	private PostLikeRepository postLikeRepository;
	private UserService userService;

	@BeforeEach
	void setUp() {
		userRepository = Mockito.mock(UserRepository.class);
		postRepository = Mockito.mock(PostRepository.class);
		commentRepository = Mockito.mock(CommentRepository.class);
		postLikeRepository = Mockito.mock(PostLikeRepository.class);
		userService = new UserService(userRepository, postRepository, commentRepository, postLikeRepository);
	}

	@Test
	void get_current_user_returns_dto_for_existing_user() {
		User user = User.createFromNaver("nv-x", "초록고추X", null);
		Mockito.when(userRepository.findById(7L)).thenReturn(Optional.of(user));

		CurrentUserResponse response = userService.getCurrentUser(7L);

		assertThat(response.nickname()).isEqualTo("초록고추X");
		assertThat(response.tier()).isEqualTo("seed");
		assertThat(response.points()).isZero();
	}

	@Test
	void get_current_user_throws_unauthorized_when_missing() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getCurrentUser(99L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	@Test
	void get_stats_aggregates_real_counts_from_repositories() {
		Mockito.when(userRepository.findById(1L))
				.thenReturn(Optional.of(User.createFromNaver("n", "n", null)));
		Mockito.when(postRepository.countByAuthorId(1L)).thenReturn(7L);
		Mockito.when(commentRepository.countActiveByAuthorId(1L)).thenReturn(23L);
		Mockito.when(postLikeRepository.countByUserId(1L)).thenReturn(41L);

		UserStatsResponse stats = userService.getStats(1L);

		assertThat(stats.postCount()).isEqualTo(7L);
		assertThat(stats.commentCount()).isEqualTo(23L);
		assertThat(stats.likeGivenCount()).isEqualTo(41L);
		assertThat(stats.attendanceStreak()).isZero();
	}
}
