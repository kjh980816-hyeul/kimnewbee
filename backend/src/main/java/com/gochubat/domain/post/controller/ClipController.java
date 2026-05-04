package com.gochubat.domain.post.controller;

import com.gochubat.domain.post.dto.ClipDetailResponse;
import com.gochubat.domain.post.dto.ClipListItemResponse;
import com.gochubat.domain.post.dto.ClipWriteRequest;
import com.gochubat.domain.post.dto.ListResponse;
import com.gochubat.domain.post.entity.BoardType;
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
@RequestMapping("/api/clips")
public class ClipController extends AuthenticatedController {

	private final PostService postService;
	private final PostBoardAssembler assembler;

	public ClipController(PostService postService, PostBoardAssembler assembler) {
		this.postService = postService;
		this.assembler = assembler;
	}

	@GetMapping
	public ListResponse<ClipListItemResponse> list() {
		List<Post> posts = postService.list(BoardType.CLIP);
		Map<Long, ListCounts> counts = assembler.gatherListCounts(posts);
		return ListResponse.of(
				posts.stream()
						.map(p -> ClipListItemResponse.from(p, counts.getOrDefault(p.getId(), ListCounts.zero())))
						.toList()
		);
	}

	@GetMapping("/{id}")
	public ClipDetailResponse detail(@PathVariable Long id, Authentication authentication) {
		Post post = postService.viewDetail(BoardType.CLIP, id);
		DetailCounts counts = assembler.gatherDetailCounts(post.getId(), currentUserIdOrNull(authentication));
		return ClipDetailResponse.from(post, counts);
	}

	@PostMapping
	public ResponseEntity<ClipDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody ClipWriteRequest request
	) {
		User author = postService.loadAuthor(requireUserId(authentication));
		Post saved = postService.save(Post.createClip(request.title(), request.description(), author, request.videoUrl()));
		return ResponseEntity.status(HttpStatus.CREATED).body(ClipDetailResponse.from(saved, DetailCounts.zero()));
	}
}
