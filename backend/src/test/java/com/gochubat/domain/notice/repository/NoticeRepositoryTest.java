package com.gochubat.domain.notice.repository;

import com.gochubat.domain.notice.entity.Notice;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NoticeRepositoryTest {

	@Autowired
	private NoticeRepository noticeRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void find_all_with_author_orders_by_created_desc_and_eager_loads_author() {
		User owner = userRepository.save(TestUserFactory.create("nv-owner", "밭주인", Tier.OWNER));
		Notice older = noticeRepository.save(Notice.create("이전 공지", "오래전 내용", owner));
		Notice newer = noticeRepository.save(Notice.create("최신 공지", "방금 내용", owner));

		List<Notice> result = noticeRepository.findAllWithAuthor();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(newer.getId());
		assertThat(result.get(1).getId()).isEqualTo(older.getId());
		assertThat(result.get(0).getAuthor().getNickname()).isEqualTo("밭주인");
	}

	@Test
	void find_by_id_with_author_returns_present_when_exists() {
		User owner = userRepository.save(TestUserFactory.create("nv-owner-2", "주인장", Tier.OWNER));
		Notice notice = noticeRepository.save(Notice.create("제목", "본문", owner));

		Optional<Notice> found = noticeRepository.findByIdWithAuthor(notice.getId());

		assertThat(found).isPresent();
		assertThat(found.get().getAuthor().getNickname()).isEqualTo("주인장");
	}

	@Test
	void find_by_id_with_author_returns_empty_when_unknown() {
		assertThat(noticeRepository.findByIdWithAuthor(99999L)).isEmpty();
	}
}
