package com.gochubat.domain.post.controller;

import com.gochubat.domain.post.dto.FreePostDetailResponse;
import com.gochubat.domain.post.dto.FreePostListItemResponse;
import com.gochubat.domain.post.dto.FreePostWriteRequest;
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
@RequestMapping("/api/free")
public class FreePostController extends AuthenticatedController {

	private final PostService postService;
	private final PostBoardAssembler assembler;

	public FreePostController(PostService postService, PostBoardAssembler assembler) {
		this.postService = postService;
		this.assembler = assembler;
	}

	@GetMapping
	public ListResponse<FreePostListItemResponse> list() {
		List<Post> posts = postService.list(BoardType.FREE);
		Map<Long, ListCounts> counts = assembler.gatherListCounts(posts);
		return ListResponse.of(
				posts.stream()
						.map(p -> FreePostListItemResponse.from(p, counts.getOrDefault(p.getId(), ListCounts.zero())))
						.toList()
		);
	}

	@GetMapping("/{id}")
	public FreePostDetailResponse detail(@PathVariable Long id, Authentication authentication) {
		Post post = postService.viewDetail(BoardType.FREE, id);
		DetailCounts counts = assembler.gatherDetailCounts(post.getId(), currentUserIdOrNull(authentication));
		return FreePostDetailResponse.from(post, counts);
	}

	@PostMapping
	public ResponseEntity<FreePostDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody FreePostWriteRequest request
	) {
		User author = postService.loadAuthor(requireUserId(authentication));
		Post saved = postService.save(Post.createFree(request.title(), request.content(), author));
		return ResponseEntity.status(HttpStatus.CREATED).body(FreePostDetailResponse.from(saved, DetailCounts.zero()));
	}
}
