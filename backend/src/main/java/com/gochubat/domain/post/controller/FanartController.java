package com.gochubat.domain.post.controller;

import com.gochubat.domain.post.dto.FanartDetailResponse;
import com.gochubat.domain.post.dto.FanartListItemResponse;
import com.gochubat.domain.post.dto.FanartWriteRequest;
import com.gochubat.domain.post.dto.ListResponse;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostService;
import com.gochubat.domain.user.entity.User;
import com.gochubat.global.security.AuthenticatedController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fanart")
public class FanartController extends AuthenticatedController {

	private final PostService postService;

	public FanartController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public ListResponse<FanartListItemResponse> list() {
		return ListResponse.of(
				postService.list(BoardType.FANART).stream()
						.map(FanartListItemResponse::from)
						.toList()
		);
	}

	@GetMapping("/{id}")
	public FanartDetailResponse detail(@PathVariable Long id) {
		return FanartDetailResponse.from(postService.viewDetail(BoardType.FANART, id));
	}

	@PostMapping
	public ResponseEntity<FanartDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody FanartWriteRequest request
	) {
		User author = postService.loadAuthor(requireUserId(authentication));
		Post saved = postService.save(Post.createFanart(request.title(), request.content(), author, request.imageUrl()));
		return ResponseEntity.status(HttpStatus.CREATED).body(FanartDetailResponse.from(saved));
	}
}
