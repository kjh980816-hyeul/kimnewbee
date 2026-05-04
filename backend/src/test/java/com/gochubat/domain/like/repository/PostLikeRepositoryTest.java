package com.gochubat.domain.like.repository;

import com.gochubat.domain.like.entity.PostLike;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PostLikeRepositoryTest {

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void unique_constraint_blocks_duplicate_post_user() {
		userRepository.save(TestUserFactory.create("nv-l", "L", Tier.PEPPER));
		postLikeRepository.save(PostLike.create(1L, 1L));
		postLikeRepository.flush();

		assertThatThrownBy(() -> {
			postLikeRepository.save(PostLike.create(1L, 1L));
			postLikeRepository.flush();
		}).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void count_by_post_id_returns_actual_count() {
		postLikeRepository.save(PostLike.create(10L, 1L));
		postLikeRepository.save(PostLike.create(10L, 2L));
		postLikeRepository.save(PostLike.create(20L, 1L));

		assertThat(postLikeRepository.countByPostId(10L)).isEqualTo(2L);
		assertThat(postLikeRepository.countByPostId(20L)).isEqualTo(1L);
		assertThat(postLikeRepository.countByPostId(30L)).isZero();
	}

	@Test
	void count_by_post_ids_batch_returns_per_post() {
		postLikeRepository.save(PostLike.create(10L, 1L));
		postLikeRepository.save(PostLike.create(10L, 2L));
		postLikeRepository.save(PostLike.create(20L, 1L));

		Map<Long, Long> result = new HashMap<>();
		postLikeRepository.countByPostIds(List.of(10L, 20L, 30L))
				.forEach(row -> result.put(row.getPostId(), row.getCnt()));

		assertThat(result).containsEntry(10L, 2L);
		assertThat(result).containsEntry(20L, 1L);
		assertThat(result).doesNotContainKey(30L);
	}

}
