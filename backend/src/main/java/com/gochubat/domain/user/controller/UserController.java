package com.gochubat.domain.user.controller;

import com.gochubat.domain.user.dto.AdminViewerInfoResponse;
import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.MyActivityItemResponse;
import com.gochubat.domain.user.dto.NicknameUpdateRequest;
import com.gochubat.domain.user.dto.ProfileImageUpdateRequest;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.service.UserService;
import com.gochubat.global.security.AuthenticatedController;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class UserController extends AuthenticatedController {

	private static final String OWNER_AUTHORITY = "ROLE_OWNER";

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public CurrentUserResponse me(Authentication authentication) {
		return userService.getCurrentUser(requireUserId(authentication));
	}

	@PatchMapping("/profile-image")
	public CurrentUserResponse updateProfileImage(
			Authentication authentication,
			@Valid @RequestBody ProfileImageUpdateRequest request
	) {
		return userService.updateProfileImage(requireUserId(authentication), request.profileImage());
	}

	@PatchMapping("/nickname")
	public CurrentUserResponse updateNickname(
			Authentication authentication,
			@Valid @RequestBody NicknameUpdateRequest request
	) {
		return userService.updateNickname(requireUserId(authentication), request.nickname());
	}

	@GetMapping("/stats")
	public UserStatsResponse stats(Authentication authentication) {
		return userService.getStats(requireUserId(authentication));
	}

	@GetMapping("/posts")
	public List<MyActivityItemResponse> myPosts(Authentication authentication) {
		return userService.getMyPosts(requireUserId(authentication));
	}

	@GetMapping("/commented")
	public List<MyActivityItemResponse> commentedPosts(Authentication authentication) {
		return userService.getCommentedPosts(requireUserId(authentication));
	}

	@GetMapping("/liked")
	public List<MyActivityItemResponse> likedPosts(Authentication authentication) {
		return userService.getLikedPosts(requireUserId(authentication));
	}

	@GetMapping("/admin")
	public AdminViewerInfoResponse admin(Authentication authentication) {
		boolean isAdmin = authentication != null
				&& authentication.getAuthorities() != null
				&& authentication.getAuthorities().stream()
				.anyMatch(a -> OWNER_AUTHORITY.equals(a.getAuthority()));
		return AdminViewerInfoResponse.of(isAdmin);
	}
}
