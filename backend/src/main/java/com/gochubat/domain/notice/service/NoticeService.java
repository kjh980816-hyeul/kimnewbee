package com.gochubat.domain.notice.service;

import com.gochubat.domain.notice.dto.NoticeDetailResponse;
import com.gochubat.domain.notice.dto.NoticeListItemResponse;
import com.gochubat.domain.notice.dto.NoticeListResponse;
import com.gochubat.domain.notice.dto.NoticeWriteRequest;
import com.gochubat.domain.notice.entity.Notice;
import com.gochubat.domain.notice.repository.NoticeRepository;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NoticeService {

	private final NoticeRepository noticeRepository;
	private final UserRepository userRepository;

	public NoticeService(NoticeRepository noticeRepository, UserRepository userRepository) {
		this.noticeRepository = noticeRepository;
		this.userRepository = userRepository;
	}

	public NoticeListResponse list() {
		return NoticeListResponse.of(
				noticeRepository.findAllWithAuthor().stream()
						.map(NoticeListItemResponse::from)
						.toList()
		);
	}

	@Transactional
	public NoticeDetailResponse detail(Long id) {
		Notice notice = noticeRepository.findByIdWithAuthor(id)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		notice.incrementViewCount();
		return NoticeDetailResponse.from(notice);
	}

	@Transactional
	public NoticeDetailResponse create(Long authorId, NoticeWriteRequest request) {
		User author = userRepository.findById(authorId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		Notice saved = noticeRepository.save(Notice.create(request.title(), request.content(), author));
		return NoticeDetailResponse.from(saved);
	}

	@Transactional
	public NoticeDetailResponse update(Long id, NoticeWriteRequest request) {
		Notice notice = noticeRepository.findByIdWithAuthor(id)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		notice.update(request.title(), request.content());
		return NoticeDetailResponse.from(notice);
	}

	@Transactional
	public void delete(Long id) {
		if (!noticeRepository.existsById(id)) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}
		noticeRepository.deleteById(id);
	}
}
