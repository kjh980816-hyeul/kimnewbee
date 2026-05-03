package com.gochubat.domain.user.controller;

import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.service.UserService;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public CurrentUserResponse me(Authentication authentication) {
		return userService.getCurrentUser(requireUserId(authentication));
	}

	@GetMapping("/stats")
	public UserStatsResponse stats(Authentication authentication) {
		return userService.getStats(requireUserId(authentication));
	}

	private Long requireUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
		return userId;
	}
}
