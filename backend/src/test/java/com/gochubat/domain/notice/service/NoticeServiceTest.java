package com.gochubat.domain.notice.service;

import com.gochubat.domain.notice.dto.NoticeDetailResponse;
import com.gochubat.domain.notice.dto.NoticeListResponse;
import com.gochubat.domain.notice.dto.NoticeWriteRequest;
import com.gochubat.domain.notice.entity.Notice;
import com.gochubat.domain.notice.repository.NoticeRepository;
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
import static org.mockito.ArgumentMatchers.any;

class NoticeServiceTest {

	private NoticeRepository noticeRepository;
	private UserRepository userRepository;
	private NoticeService noticeService;
	private User owner;

	@BeforeEach
	void setUp() {
		noticeRepository = Mockito.mock(NoticeRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		noticeService = new NoticeService(noticeRepository, userRepository);
		owner = TestUserFactory.create("nv-o", "주인", Tier.OWNER);
		TestUserFactory.setId(owner, 1L);
	}

	@Test
	void list_returns_dto_for_each_notice() {
		Mockito.when(noticeRepository.findAllWithAuthor()).thenReturn(List.of(
				Notice.create("A", "a-body", owner),
				Notice.create("B", "b-body", owner)
		));

		NoticeListResponse response = noticeService.list();

		assertThat(response.total()).isEqualTo(2);
		assertThat(response.data()).extracting("title").containsExactly("A", "B");
		assertThat(response.data()).allMatch(item -> "주인".equals(item.author()));
	}

	@Test
	void detail_increments_view_count_and_returns_dto() {
		Notice notice = Notice.create("공지", "본문", owner);
		Mockito.when(noticeRepository.findByIdWithAuthor(7L)).thenReturn(Optional.of(notice));

		NoticeDetailResponse response = noticeService.detail(7L);

		assertThat(response.viewCount()).isEqualTo(1L);
		assertThat(notice.getViewCount()).isEqualTo(1L);
		assertThat(response.content()).isEqualTo("본문");
	}

	@Test
	void detail_throws_not_found_when_missing() {
		Mockito.when(noticeRepository.findByIdWithAuthor(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noticeService.detail(99L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	void create_persists_notice_with_author() {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
		Mockito.when(noticeRepository.save(any(Notice.class))).thenAnswer(inv -> inv.getArgument(0));

		NoticeDetailResponse response = noticeService.create(1L, new NoticeWriteRequest("새 공지", "내용"));

		assertThat(response.title()).isEqualTo("새 공지");
		assertThat(response.author()).isEqualTo("주인");
		Mockito.verify(noticeRepository).save(any(Notice.class));
	}

	@Test
	void create_throws_unauthorized_when_user_missing() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noticeService.create(99L, new NoticeWriteRequest("t", "c")))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	@Test
	void update_modifies_existing_notice() {
		Notice notice = Notice.create("이전", "이전 본문", owner);
		Mockito.when(noticeRepository.findByIdWithAuthor(3L)).thenReturn(Optional.of(notice));

		NoticeDetailResponse response = noticeService.update(3L, new NoticeWriteRequest("수정", "수정 본문"));

		assertThat(response.title()).isEqualTo("수정");
		assertThat(notice.getTitle()).isEqualTo("수정");
		assertThat(notice.getContent()).isEqualTo("수정 본문");
	}

	@Test
	void update_throws_not_found_when_missing() {
		Mockito.when(noticeRepository.findByIdWithAuthor(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noticeService.update(404L, new NoticeWriteRequest("t", "c")))
				.isInstanceOf(CustomException.class);
	}

	@Test
	void delete_removes_when_exists() {
		Mockito.when(noticeRepository.existsById(5L)).thenReturn(true);

		noticeService.delete(5L);

		Mockito.verify(noticeRepository).deleteById(5L);
	}

	@Test
	void delete_throws_not_found_when_missing() {
		Mockito.when(noticeRepository.existsById(404L)).thenReturn(false);

		assertThatThrownBy(() -> noticeService.delete(404L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NOT_FOUND);
		Mockito.verify(noticeRepository, Mockito.never()).deleteById(any());
	}
}
