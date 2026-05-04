package com.gochubat.domain.letter.repository;

import com.gochubat.domain.letter.entity.Letter;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LetterRepositoryTest {

	@Autowired
	private LetterRepository letterRepository;

	@Autowired
	private UserRepository userRepository;

	private User user;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-letter", "팬", Tier.PEPPER));
	}

	@Test
	void find_all_with_author_orders_by_created_desc() {
		Letter first = letterRepository.save(Letter.create(user, "첫편지"));
		Letter second = letterRepository.save(Letter.create(user, "두번째"));

		List<Letter> result = letterRepository.findAllWithAuthor();

		assertThat(result).extracting(Letter::getId).containsExactly(second.getId(), first.getId());
		assertThat(result.get(0).getAuthor().getNickname()).isEqualTo("팬");
	}

	@Test
	void find_by_id_with_author_returns_present() {
		Letter saved = letterRepository.save(Letter.create(user, "본문 짧음"));

		Optional<Letter> found = letterRepository.findByIdWithAuthor(saved.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getAuthor().getNickname()).isEqualTo("팬");
	}

	@Test
	void find_by_id_with_author_returns_empty_when_unknown() {
		assertThat(letterRepository.findByIdWithAuthor(99999L)).isEmpty();
	}

	@Test
	void preview_truncates_long_content() {
		Letter longLetter = Letter.create(user, "x".repeat(50));

		assertThat(longLetter.preview()).hasSize(33).endsWith("...");
	}

	@Test
	void preview_returns_full_for_short_content() {
		Letter shortLetter = Letter.create(user, "짧은 편지에요");

		assertThat(shortLetter.preview()).isEqualTo("짧은 편지에요");
	}
}
