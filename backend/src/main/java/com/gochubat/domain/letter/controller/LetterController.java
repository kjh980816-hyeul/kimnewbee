package com.gochubat.domain.letter.controller;

import com.gochubat.domain.letter.dto.LetterDetailResponse;
import com.gochubat.domain.letter.dto.LetterListItemResponse;
import com.gochubat.domain.letter.dto.LetterWriteRequest;
import com.gochubat.domain.letter.service.LetterService;
import com.gochubat.domain.post.dto.ListResponse;
import com.gochubat.global.security.AuthenticatedController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/letters")
public class LetterController extends AuthenticatedController {

	private final LetterService letterService;

	public LetterController(LetterService letterService) {
		this.letterService = letterService;
	}

	@GetMapping
	public ListResponse<LetterListItemResponse> list() {
		return ListResponse.of(letterService.list());
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('OWNER')")
	public LetterDetailResponse detail(@PathVariable Long id) {
		return letterService.detailForAdmin(id);
	}

	@PostMapping
	public ResponseEntity<LetterDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody LetterWriteRequest request
	) {
		LetterDetailResponse response = letterService.create(requireUserId(authentication), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
