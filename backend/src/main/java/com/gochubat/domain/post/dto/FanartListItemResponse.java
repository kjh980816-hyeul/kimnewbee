package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;

import java.time.LocalDateTime;

public record FanartListItemResponse(
		Long id,
		String title,
		String author,
		String thumbnailUrl,
		LocalDateTime createdAt,
		long likeCount,
		long commentCount
) {

	public static FanartListItemResponse from(Post post) {
		return new FanartListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getMediaUrl(),
				post.getCreatedAt(),
				0L,
				0L
		);
	}
}
