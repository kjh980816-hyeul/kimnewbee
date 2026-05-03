package com.gochubat.domain.notice.controller;

import com.gochubat.domain.notice.dto.NoticeDetailResponse;
import com.gochubat.domain.notice.dto.NoticeListResponse;
import com.gochubat.domain.notice.dto.NoticeWriteRequest;
import com.gochubat.domain.notice.service.NoticeService;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

	private final NoticeService noticeService;

	public NoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	@GetMapping
	public NoticeListResponse list() {
		return noticeService.list();
	}

	@GetMapping("/{id}")
	public NoticeDetailResponse detail(@PathVariable Long id) {
		return noticeService.detail(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('OWNER')")
	public ResponseEntity<NoticeDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody NoticeWriteRequest request
	) {
		Long authorId = requireUserId(authentication);
		return ResponseEntity.status(HttpStatus.CREATED).body(noticeService.create(authorId, request));
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasRole('OWNER')")
	public NoticeDetailResponse update(@PathVariable Long id, @Valid @RequestBody NoticeWriteRequest request) {
		return noticeService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('OWNER')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		noticeService.delete(id);
		return ResponseEntity.noContent().build();
	}

	private Long requireUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
		return userId;
	}
}
