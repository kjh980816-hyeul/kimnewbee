package com.gochubat.domain.post.controller;

import com.gochubat.domain.post.dto.OfflineDetailResponse;
import com.gochubat.domain.post.dto.OfflineListItemResponse;
import com.gochubat.domain.post.dto.OfflineWriteRequest;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.global.dto.ListResponse;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler;
import com.gochubat.domain.post.service.PostBoardAssembler.DetailCounts;
import com.gochubat.domain.post.service.PostBoardAssembler.ListCounts;
import com.gochubat.domain.post.service.PostService;
import com.gochubat.domain.user.entity.User;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offline")
@PreAuthorize("hasAnyRole('CORN','OWNER')")
public class OfflineController extends AuthenticatedController {

	private final PostService postService;
	private final PostBoardAssembler assembler;

	public OfflineController(PostService postService, PostBoardAssembler assembler) {
		this.postService = postService;
		this.assembler = assembler;
	}

	@GetMapping
	public ListResponse<OfflineListItemResponse> list() {
		List<Post> posts = postService.list(BoardType.OFFLINE);
		Map<Long, ListCounts> counts = assembler.gatherListCounts(posts);
		return ListResponse.of(
				posts.stream()
						.map(p -> OfflineListItemResponse.from(p, counts.getOrDefault(p.getId(), ListCounts.zero())))
						.toList()
		);
	}

	@GetMapping("/{id}")
	public OfflineDetailResponse detail(@PathVariable Long id, Authentication authentication) {
		Post post = postService.viewDetail(BoardType.OFFLINE, id);
		DetailCounts counts = assembler.gatherDetailCounts(post.getId(), currentUserIdOrNull(authentication));
		return OfflineDetailResponse.from(post, counts);
	}

	@PostMapping
	public ResponseEntity<OfflineDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody OfflineWriteRequest request
	) {
		User author = postService.loadAuthor(requireUserId(authentication));
		Post saved = postService.save(Post.createOffline(
				request.title(),
				request.content(),
				author,
				request.imageUrl(),
				request.location(),
				request.meetupDate()
		));
		return ResponseEntity.status(HttpStatus.CREATED).body(OfflineDetailResponse.from(saved, DetailCounts.zero()));
	}
}
