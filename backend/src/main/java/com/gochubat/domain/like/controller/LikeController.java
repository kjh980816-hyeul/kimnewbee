package com.gochubat.domain.like.controller;

import com.gochubat.domain.like.dto.LikeToggleResponse;
import com.gochubat.domain.like.service.LikeService;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.global.security.AuthenticatedController;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LikeController extends AuthenticatedController {

	private final LikeService likeService;

	public LikeController(LikeService likeService) {
		this.likeService = likeService;
	}

	@PostMapping("/api/free/{id}/like")
	public LikeToggleResponse toggleFree(@PathVariable Long id, Authentication authentication) {
		return likeService.toggle(BoardType.FREE, id, requireUserId(authentication));
	}

	@PostMapping("/api/fanart/{id}/like")
	public LikeToggleResponse toggleFanart(@PathVariable Long id, Authentication authentication) {
		return likeService.toggle(BoardType.FANART, id, requireUserId(authentication));
	}

	@PostMapping("/api/clips/{id}/like")
	public LikeToggleResponse toggleClip(@PathVariable Long id, Authentication authentication) {
		return likeService.toggle(BoardType.CLIP, id, requireUserId(authentication));
	}
}
