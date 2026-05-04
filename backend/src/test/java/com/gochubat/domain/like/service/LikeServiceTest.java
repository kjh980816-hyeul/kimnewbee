package com.gochubat.domain.like.service;

import com.gochubat.domain.like.dto.LikeToggleResponse;
import com.gochubat.domain.like.entity.PostLike;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

class LikeServiceTest {

	private PostLikeRepository postLikeRepository;
	private PostRepository postRepository;
	private LikeService likeService;
	private User author;

	@BeforeEach
	void setUp() {
		postLikeRepository = Mockito.mock(PostLikeRepository.class);
		postRepository = Mockito.mock(PostRepository.class);
		likeService = new LikeService(postLikeRepository, postRepository);
		author = TestUserFactory.create("nv-a", "A", Tier.PEPPER);
		TestUserFactory.setId(author, 1L);
	}

	@Test
	void toggle_throws_not_found_when_post_missing() {
		Mockito.when(postRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> likeService.toggle(BoardType.FREE, 99L, 1L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	void toggle_throws_not_found_when_board_type_mismatch() {
		Post fanart = Post.createFanart("팬아트", "본문", author, "https://img");
		Mockito.when(postRepository.findById(5L)).thenReturn(Optional.of(fanart));

		assertThatThrownBy(() -> likeService.toggle(BoardType.FREE, 5L, 1L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	void toggle_creates_new_like_when_none_exists() {
		Post free = Post.createFree("자유", "본문", author);
		Mockito.when(postRepository.findById(5L)).thenReturn(Optional.of(free));
		Mockito.when(postLikeRepository.findByPostIdAndUserId(5L, 1L)).thenReturn(Optional.empty());
		Mockito.when(postLikeRepository.countByPostId(5L)).thenReturn(1L);

		LikeToggleResponse response = likeService.toggle(BoardType.FREE, 5L, 1L);

		assertThat(response.liked()).isTrue();
		assertThat(response.likeCount()).isEqualTo(1L);
		Mockito.verify(postLikeRepository).save(any(PostLike.class));
		Mockito.verify(postLikeRepository, Mockito.never()).delete(any());
	}

	@Test
	void toggle_removes_existing_like() {
		Post free = Post.createFree("자유", "본문", author);
		PostLike existing = PostLike.create(5L, 1L);
		Mockito.when(postRepository.findById(5L)).thenReturn(Optional.of(free));
		Mockito.when(postLikeRepository.findByPostIdAndUserId(5L, 1L)).thenReturn(Optional.of(existing));
		Mockito.when(postLikeRepository.countByPostId(5L)).thenReturn(0L);

		LikeToggleResponse response = likeService.toggle(BoardType.FREE, 5L, 1L);

		assertThat(response.liked()).isFalse();
		assertThat(response.likeCount()).isZero();
		Mockito.verify(postLikeRepository).delete(existing);
		Mockito.verify(postLikeRepository, Mockito.never()).save(any());
	}

}
