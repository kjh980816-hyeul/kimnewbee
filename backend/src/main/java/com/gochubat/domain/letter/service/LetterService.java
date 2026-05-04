package com.gochubat.domain.letter.service;

import com.gochubat.domain.letter.dto.LetterDetailResponse;
import com.gochubat.domain.letter.dto.LetterListItemResponse;
import com.gochubat.domain.letter.dto.LetterWriteRequest;
import com.gochubat.domain.letter.entity.Letter;
import com.gochubat.domain.letter.repository.LetterRepository;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LetterService {

	private final LetterRepository letterRepository;
	private final UserRepository userRepository;

	public LetterService(LetterRepository letterRepository, UserRepository userRepository) {
		this.letterRepository = letterRepository;
		this.userRepository = userRepository;
	}

	public List<LetterListItemResponse> list() {
		return letterRepository.findAllWithAuthor().stream()
				.map(LetterListItemResponse::from)
				.toList();
	}

	@Transactional
	public LetterDetailResponse detailForAdmin(Long letterId) {
		Letter letter = letterRepository.findByIdWithAuthor(letterId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		letter.markReadByAdmin();
		return LetterDetailResponse.from(letter);
	}

	@Transactional
	public LetterDetailResponse create(Long userId, LetterWriteRequest request) {
		User author = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		Letter saved = letterRepository.save(Letter.create(author, request.content()));
		return LetterDetailResponse.from(saved);
	}
}
