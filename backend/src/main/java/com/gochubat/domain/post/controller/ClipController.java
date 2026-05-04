package com.gochubat.domain.post.controller;

import com.gochubat.domain.comment.service.CommentService;
import com.gochubat.domain.post.dto.ClipDetailResponse;
import com.gochubat.domain.post.dto.ClipListItemResponse;
import com.gochubat.domain.post.dto.ClipWriteRequest;
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
@RequestMapping("/api/clips")
public class ClipController extends AuthenticatedController {

	private final PostService postService;
	private final CommentService commentService;

	public ClipController(PostService postService, CommentService commentService) {
		this.postService = postService;
		this.commentService = commentService;
	}

	@GetMapping
	public ListResponse<ClipListItemResponse> list() {
		List<Post> posts = postService.list(BoardType.CLIP);
		Map<Long, Long> counts = commentService.countActiveByPostIds(posts.stream().map(Post::getId).toList());
		return ListResponse.of(
				posts.stream()
						.map(p -> ClipListItemResponse.from(p, counts.getOrDefault(p.getId(), 0L)))
						.toList()
		);
	}

	@GetMapping("/{id}")
	public ClipDetailResponse detail(@PathVariable Long id) {
		Post post = postService.viewDetail(BoardType.CLIP, id);
		return ClipDetailResponse.from(post, commentService.countActive(post.getId()));
	}

	@PostMapping
	public ResponseEntity<ClipDetailResponse> create(
			Authentication authentication,
			@Valid @RequestBody ClipWriteRequest request
	) {
		User author = postService.loadAuthor(requireUserId(authentication));
		Post saved = postService.save(Post.createClip(request.title(), request.description(), author, request.videoUrl()));
		return ResponseEntity.status(HttpStatus.CREATED).body(ClipDetailResponse.from(saved, 0L));
	}
}
