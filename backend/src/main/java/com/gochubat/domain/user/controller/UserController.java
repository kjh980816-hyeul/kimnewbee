package com.gochubat.domain.user.controller;

import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.service.UserService;
import com.gochubat.global.security.AuthenticatedController;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class UserController extends AuthenticatedController {

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
}
