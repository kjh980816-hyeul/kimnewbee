package com.gochubat.domain.comment.controller;

import com.gochubat.domain.comment.dto.CommentResponse;
import com.gochubat.domain.comment.dto.CommentWriteRequest;
import com.gochubat.domain.comment.service.CommentService;
import com.gochubat.global.security.AuthenticatedController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommentController extends AuthenticatedController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping("/api/posts/{postId}/comments")
	public List<CommentResponse> list(@PathVariable Long postId) {
		return commentService.list(postId);
	}

	@PostMapping("/api/posts/{postId}/comments")
	public ResponseEntity<CommentResponse> create(
			@PathVariable Long postId,
			Authentication authentication,
			@Valid @RequestBody CommentWriteRequest request
	) {
		CommentResponse response = commentService.create(postId, requireUserId(authentication), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/api/comments/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
		commentService.softDelete(id, requireUserId(authentication));
		return ResponseEntity.noContent().build();
	}
}
