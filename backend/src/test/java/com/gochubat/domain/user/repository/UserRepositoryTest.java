package com.gochubat.domain.user.repository;

import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	void persists_with_default_tier_seed_and_zero_points() {
		User saved = userRepository.save(User.createFromNaver("nv-1", "초록고추1", "https://img/1.png"));

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getTier()).isEqualTo(Tier.SEED);
		assertThat(saved.getPoints()).isZero();
		assertThat(saved.getCreatedAt()).isNotNull();
	}

	@Test
	void find_by_naver_id_returns_existing_user() {
		userRepository.save(User.createFromNaver("nv-2", "초록고추2", null));

		Optional<User> found = userRepository.findByNaverId("nv-2");

		assertThat(found).isPresent();
		assertThat(found.get().getNickname()).isEqualTo("초록고추2");
	}

	@Test
	void find_by_naver_id_returns_empty_when_unknown() {
		assertThat(userRepository.findByNaverId("unknown")).isEmpty();
	}
}
