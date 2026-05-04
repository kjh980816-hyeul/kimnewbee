package com.gochubat.domain.letter.service;

import com.gochubat.domain.letter.dto.LetterDetailResponse;
import com.gochubat.domain.letter.dto.LetterWriteRequest;
import com.gochubat.domain.letter.entity.Letter;
import com.gochubat.domain.letter.repository.LetterRepository;
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

class LetterServiceTest {

	private LetterRepository letterRepository;
	private UserRepository userRepository;
	private LetterService letterService;
	private User user;

	@BeforeEach
	void setUp() {
		letterRepository = Mockito.mock(LetterRepository.class);
		userRepository = Mockito.mock(UserRepository.class);
		letterService = new LetterService(letterRepository, userRepository);
		user = TestUserFactory.create("nv-x", "팬", Tier.PEPPER);
		TestUserFactory.setId(user, 1L);
	}

	@Test
	void create_persists_with_author() {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		Mockito.when(letterRepository.save(any(Letter.class))).thenAnswer(inv -> inv.getArgument(0));

		LetterDetailResponse response = letterService.create(1L, new LetterWriteRequest("내 편지"));

		assertThat(response.author()).isEqualTo("팬");
		assertThat(response.content()).isEqualTo("내 편지");
		assertThat(response.isReadByAdmin()).isFalse();
	}

	@Test
	void create_throws_unauthorized_when_user_missing() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> letterService.create(99L, new LetterWriteRequest("x")))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.UNAUTHORIZED);
	}

	@Test
	void detail_for_admin_marks_read_and_returns_dto() {
		Letter letter = Letter.create(user, "관리자가 읽을 편지");
		Mockito.when(letterRepository.findByIdWithAuthor(7L)).thenReturn(Optional.of(letter));

		LetterDetailResponse response = letterService.detailForAdmin(7L);

		assertThat(response.isReadByAdmin()).isTrue();
		assertThat(letter.isReadByAdmin()).isTrue();
	}

	@Test
	void detail_for_admin_throws_not_found_when_missing() {
		Mockito.when(letterRepository.findByIdWithAuthor(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> letterService.detailForAdmin(99L))
				.isInstanceOf(CustomException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.NOT_FOUND);
	}

	@Test
	void detail_for_admin_idempotent_when_already_read() {
		Letter letter = Letter.create(user, "이미 읽은 편지");
		letter.markReadByAdmin();
		Mockito.when(letterRepository.findByIdWithAuthor(3L)).thenReturn(Optional.of(letter));

		LetterDetailResponse first = letterService.detailForAdmin(3L);
		LetterDetailResponse second = letterService.detailForAdmin(3L);

		assertThat(first.isReadByAdmin()).isTrue();
		assertThat(second.isReadByAdmin()).isTrue();
	}
}
