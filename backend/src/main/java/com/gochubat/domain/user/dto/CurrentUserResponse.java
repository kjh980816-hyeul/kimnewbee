package com.gochubat.domain.user.dto;

import com.gochubat.domain.user.entity.User;

import java.time.LocalDateTime;

public record CurrentUserResponse(
		Long id,
		String nickname,
		String tier,
		long points,
		String profileImage,
		LocalDateTime createdAt
) {

	public static CurrentUserResponse from(User user) {
		return new CurrentUserResponse(
				user.getId(),
				user.getNickname(),
				user.getTier().toApiValue(),
				user.getPoints(),
				user.getProfileImage(),
				user.getCreatedAt()
		);
	}
}
