package com.gochubat.domain.post.controller;

import com.gochubat.domain.post.service.PostService;
import com.gochubat.global.security.AuthenticatedController;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController extends AuthenticatedController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(Authentication authentication, @PathVariable Long id) {
		postService.deleteByRequester(id, requireUserId(authentication), hasOwnerRole(authentication));
	}
}
