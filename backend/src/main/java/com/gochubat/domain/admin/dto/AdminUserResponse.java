package com.gochubat.domain.admin.dto;

import com.gochubat.domain.user.entity.User;

import java.time.LocalDateTime;

public record AdminUserResponse(
		Long id,
		String naverId,
		String nickname,
		String tier,
		long points,
		LocalDateTime createdAt
) {

	public static AdminUserResponse from(User user) {
		return new AdminUserResponse(
				user.getId(),
				user.getNaverId(),
				user.getNickname(),
				user.getTier().toApiValue(),
				user.getPoints(),
				user.getCreatedAt()
		);
	}
}
