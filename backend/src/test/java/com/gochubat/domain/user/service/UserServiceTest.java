package com.gochubat.domain.user.service;

import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.MyActivityItemResponse;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
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

	@Test
	void get_my_posts_maps_each_post_to_activity_item_with_board_link() {
		Mockito.when(userRepository.findById(1L))
				.thenReturn(Optional.of(User.createFromNaver("n", "n", null)));
		Post free = postWithId(10L, Post.createFree("자유글", "내용", author()));
		Post fanart = postWithId(11L, Post.createFanart("그림", "설명", author(), "/uploads/a.png"));
		Mockito.when(postRepository.findByAuthorIdWithAuthor(1L)).thenReturn(List.of(free, fanart));
		Mockito.when(commentRepository.countActiveByPostIds(Mockito.anyList())).thenReturn(List.of());
		Mockito.when(postLikeRepository.countByPostIds(Mockito.anyList())).thenReturn(List.of());

		List<MyActivityItemResponse> items = userService.getMyPosts(1L);

		assertThat(items).hasSize(2);
		assertThat(items.get(0).link()).isEqualTo("/free/10");
		assertThat(items.get(0).boardType()).isEqualTo("FREE");
		assertThat(items.get(1).link()).isEqualTo("/fanart/11");
		assertThat(items.get(1).thumbnailUrl()).isEqualTo("/uploads/a.png");
	}

	@Test
	void get_commented_posts_preserves_comment_order_and_skips_deleted_posts() {
		Mockito.when(userRepository.findById(1L))
				.thenReturn(Optional.of(User.createFromNaver("n", "n", null)));
		// 99L은 삭제돼 findByIdIn 결과에 없음 → 목록에서 빠져야 함
		Mockito.when(commentRepository.findDistinctPostIdsByAuthorIdOrderByLatest(1L))
				.thenReturn(List.of(30L, 20L, 99L));
		Post p30 = postWithId(30L, Post.createFree("a", "x", author()));
		Post p20 = postWithId(20L, Post.createPet("b", "y", author(), null));
		// 리포지토리는 순서를 보장하지 않으므로 일부러 뒤섞어 반환
		Mockito.when(postRepository.findByIdInWithAuthor(Mockito.any())).thenReturn(List.of(p20, p30));
		Mockito.when(commentRepository.countActiveByPostIds(Mockito.anyList())).thenReturn(List.of());
		Mockito.when(postLikeRepository.countByPostIds(Mockito.anyList())).thenReturn(List.of());

		List<MyActivityItemResponse> items = userService.getCommentedPosts(1L);

		assertThat(items).extracting(MyActivityItemResponse::id).containsExactly(30L, 20L);
		assertThat(items.get(1).link()).isEqualTo("/pets/20");
	}

	@Test
	void get_liked_posts_throws_unauthorized_when_user_missing() {
		Mockito.when(userRepository.findById(5L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getLikedPosts(5L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	private User author() {
		return User.createFromNaver("author", "작성자", null);
	}

	private Post postWithId(long id, Post post) {
		try {
			Field field = Post.class.getDeclaredField("id");
			field.setAccessible(true);
			field.set(post, id);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
		return post;
	}
}
