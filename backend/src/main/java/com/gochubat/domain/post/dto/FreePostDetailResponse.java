package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;

import java.time.LocalDateTime;

public record FreePostDetailResponse(
		Long id,
		String title,
		String content,
		String author,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		long viewCount,
		long likeCount,
		long commentCount,
		boolean likedByMe
) {

	public static FreePostDetailResponse from(Post post) {
		return new FreePostDetailResponse(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				post.getAuthor().getNickname(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getViewCount(),
				0L,
				0L,
				false
		);
	}
}
