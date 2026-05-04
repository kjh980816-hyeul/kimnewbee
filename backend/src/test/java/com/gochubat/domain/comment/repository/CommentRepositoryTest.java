package com.gochubat.domain.comment.repository;

import com.gochubat.domain.comment.entity.Comment;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private UserRepository userRepository;

	private User user;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-c", "댓글러", Tier.PEPPER));
	}

	@Test
	void find_by_post_id_returns_in_created_asc_order() {
		commentRepository.save(Comment.create(1L, null, user, "첫"));
		commentRepository.save(Comment.create(1L, null, user, "둘"));
		commentRepository.save(Comment.create(2L, null, user, "다른 글"));

		List<Comment> comments = commentRepository.findByPostIdWithAuthor(1L);

		assertThat(comments).hasSize(2);
		assertThat(comments).extracting(Comment::getContent).containsExactly("첫", "둘");
		assertThat(comments.get(0).getAuthor().getNickname()).isEqualTo("댓글러");
	}

	@Test
	void count_active_excludes_soft_deleted() {
		Comment c1 = commentRepository.save(Comment.create(1L, null, user, "살아있음"));
		commentRepository.save(Comment.create(1L, null, user, "또 살아있음"));
		Comment c3 = commentRepository.save(Comment.create(1L, null, user, "삭제될 거"));
		c3.softDelete();
		commentRepository.flush();

		assertThat(commentRepository.countActiveByPostId(1L)).isEqualTo(2L);
		assertThat(c1.isDeleted()).isFalse();
	}

	@Test
	void count_active_by_post_ids_returns_per_post_count() {
		commentRepository.save(Comment.create(10L, null, user, "a"));
		commentRepository.save(Comment.create(10L, null, user, "b"));
		commentRepository.save(Comment.create(20L, null, user, "c"));
		Comment deleted = commentRepository.save(Comment.create(20L, null, user, "x"));
		deleted.softDelete();
		commentRepository.flush();

		Map<Long, Long> result = new HashMap<>();
		commentRepository.countActiveByPostIds(List.of(10L, 20L, 30L))
				.forEach(row -> result.put(row.getPostId(), row.getCnt()));

		assertThat(result).containsEntry(10L, 2L);
		assertThat(result).containsEntry(20L, 1L);
		assertThat(result).doesNotContainKey(30L);
	}
}
