package com.gochubat.domain.post.controller;

import com.gochubat.domain.comment.service.CommentService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fanart")
public class FanartController extends AuthenticatedController {

	private final PostService postService;
	private final CommentService commentService;

	public FanartController(PostService postService, CommentService commentService) {
		this.postService = postService;
		this.commentService = commentService;
	}

	@GetMapping
	public ListResponse<FanartListItemResponse> list() {
		List<Post> posts = postService.list(BoardType.FANART);
		Map<Long, Long> counts = commentService.countActiveByPostIds(posts.stream().map(Post::getId).toList());
		return ListResponse.of(
				posts.stream()
						.map(p -> FanartListItemResponse.from(p, counts.getOrDefault(p.getId(), 0L)))
						.toList()
		);
	}

	@GetMapping("/{id}")
	public FanartDetailResponse detail(@PathVariable Long id) {
		Post post = postService.viewDetail(BoardType.FANART, id);
		return FanartDetailResponse.from(post, commentService.countActive(post.getId()));
	}

	@PostMapping
	public ResponseEntity<FanartDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody FanartWriteRequest request
	) {
		User author = postService.loadAuthor(requireUserId(authentication));
		Post saved = postService.save(Post.createFanart(request.title(), request.content(), author, request.imageUrl()));
		return ResponseEntity.status(HttpStatus.CREATED).body(FanartDetailResponse.from(saved, 0L));
	}
}
