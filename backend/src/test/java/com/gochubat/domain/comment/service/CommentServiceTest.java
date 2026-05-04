package com.gochubat.domain.comment.service;

import com.gochubat.domain.comment.dto.CommentResponse;
import com.gochubat.domain.comment.dto.CommentWriteRequest;
import com.gochubat.domain.comment.entity.Comment;
import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.point.PointService;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

class CommentServiceTest {

	private CommentRepository commentRepository;
	private PostRepository postRepository;
	private UserRepository userRepository;
	private PointService pointService;
	private CommentService commentService;
	private User author;

	@BeforeEach
	void setUp() {
		commentRepository = Mockito.mock(CommentRepository.class);
		postRepository = Mockito.mock(PostRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		pointService = Mockito.mock(PointService.class);
		commentService = new CommentService(commentRepository, postRepository, userRepository, pointService);
		author = TestUserFactory.create("nv-c", "작성자", Tier.PEPPER);
		TestUserFactory.setId(author, 1L);
	}

	@Test
	void create_throws_not_found_when_post_missing() {
		Mockito.when(postRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> commentService.create(1L, 1L, new CommentWriteRequest(null, "hi")))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	void create_persists_root_comment() {
		Mockito.when(postRepository.existsById(5L)).thenReturn(true);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(author));
		Mockito.when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

		CommentResponse response = commentService.create(5L, 1L, new CommentWriteRequest(null, "내용"));

		assertThat(response.postId()).isEqualTo(5L);
		assertThat(response.parentId()).isNull();
		assertThat(response.author()).isEqualTo("작성자");
		assertThat(response.deleted()).isFalse();
	}

	@Test
	void create_reply_validates_parent_post_match() {
		Mockito.when(postRepository.existsById(5L)).thenReturn(true);
		Comment parent = Comment.create(99L, null, author, "다른 글의 댓글");
		Mockito.when(commentRepository.findById(7L)).thenReturn(Optional.of(parent));

		assertThatThrownBy(() -> commentService.create(5L, 1L, new CommentWriteRequest(7L, "답글")))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.INVALID_REQUEST);
	}

	@Test
	void create_reply_succeeds_when_parent_belongs_to_same_post() {
		Mockito.when(postRepository.existsById(5L)).thenReturn(true);
		Comment parent = Comment.create(5L, null, author, "원댓글");
		Mockito.when(commentRepository.findById(7L)).thenReturn(Optional.of(parent));
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(author));
		Mockito.when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

		CommentResponse response = commentService.create(5L, 1L, new CommentWriteRequest(7L, "답글"));

		assertThat(response.parentId()).isEqualTo(7L);
		assertThat(response.content()).isEqualTo("답글");
	}

	@Test
	void soft_delete_throws_when_not_owner() {
		Comment comment = Comment.create(1L, null, author, "내 댓글");
		Mockito.when(commentRepository.findById(3L)).thenReturn(Optional.of(comment));

		assertThatThrownBy(() -> commentService.softDelete(3L, 999L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.FORBIDDEN);
		assertThat(comment.isDeleted()).isFalse();
	}

	@Test
	void soft_delete_marks_deleted_for_owner() {
		Comment comment = Comment.create(1L, null, author, "삭제할 댓글");
		Mockito.when(commentRepository.findById(3L)).thenReturn(Optional.of(comment));

		commentService.softDelete(3L, 1L);

		assertThat(comment.isDeleted()).isTrue();
	}

	@Test
	void list_throws_not_found_when_post_missing() {
		Mockito.when(postRepository.existsById(99L)).thenReturn(false);

		assertThatThrownBy(() -> commentService.list(99L))
				.isInstanceOf(CustomException.class);
	}
}
