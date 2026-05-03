package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;

import java.time.LocalDateTime;

public record FanartDetailResponse(
		Long id,
		String title,
		String author,
		String thumbnailUrl,
		String imageUrl,
		String content,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		long viewCount,
		long likeCount,
		long commentCount,
		boolean likedByMe
) {

	public static FanartDetailResponse from(Post post) {
		return new FanartDetailResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getMediaUrl(),
				post.getMediaUrl(),
				post.getContent(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getViewCount(),
				0L,
				0L,
				false
		);
	}
}
