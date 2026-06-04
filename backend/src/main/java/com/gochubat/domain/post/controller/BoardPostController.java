package com.gochubat.domain.post.controller;

import com.gochubat.domain.post.dto.BoardPostDetailResponse;
import com.gochubat.domain.post.dto.BoardPostListItemResponse;
import com.gochubat.domain.post.dto.BoardPostWriteRequest;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler;
import com.gochubat.domain.post.service.PostBoardAssembler.DetailCounts;
import com.gochubat.domain.post.service.PostBoardAssembler.ListCounts;
import com.gochubat.domain.post.service.PostService;
import com.gochubat.global.dto.ListResponse;
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

// 관리자가 만든 커스텀 게시판(slug 기반)의 글 목록/상세/작성.
// 기본 8개 게시판은 각자 전용 컨트롤러를 쓰고, 여기는 board_slug가 채워진 CUSTOM 글만 다룬다.
// 삭제는 공용 DELETE /api/posts/{id}(PostController)를 그대로 쓴다.
@RestController
@RequestMapping("/api/board-posts/{slug}")
public class BoardPostController extends AuthenticatedController {

	private final PostService postService;
	private final PostBoardAssembler assembler;

	public BoardPostController(PostService postService, PostBoardAssembler assembler) {
		this.postService = postService;
		this.assembler = assembler;
	}

	@GetMapping
	public ListResponse<BoardPostListItemResponse> list(@PathVariable String slug) {
		List<Post> posts = postService.listCustom(slug);
		Map<Long, ListCounts> counts = assembler.gatherListCounts(posts);
		return ListResponse.of(
				posts.stream()
						.map(p -> BoardPostListItemResponse.from(p, counts.getOrDefault(p.getId(), ListCounts.zero())))
						.toList()
		);
	}

	@GetMapping("/{id}")
	public BoardPostDetailResponse detail(
			@PathVariable String slug,
			@PathVariable Long id,
			Authentication authentication
	) {
		Post post = postService.viewDetailCustom(slug, id);
		DetailCounts counts = assembler.gatherDetailCounts(post.getId(), currentUserIdOrNull(authentication));
		return BoardPostDetailResponse.from(post, counts);
	}

	@PostMapping
	public ResponseEntity<BoardPostDetailResponse> create(
			@PathVariable String slug,
			Authentication authentication,
			@Valid @RequestBody BoardPostWriteRequest request
	) {
		Post saved = postService.createCustomPost(
				slug,
				requireUserId(authentication),
				request.title(),
				request.content(),
				request.mediaUrlOrNull()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(BoardPostDetailResponse.from(saved, DetailCounts.zero()));
	}
}
