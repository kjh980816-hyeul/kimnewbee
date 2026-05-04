package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;

import java.time.LocalDateTime;

public record ClipDetailResponse(
		Long id,
		String title,
		String author,
		String videoUrl,
		String source,
		String description,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		long viewCount,
		long likeCount,
		long commentCount,
		boolean likedByMe
) {

	public static ClipDetailResponse from(Post post, long commentCount) {
		return new ClipDetailResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getMediaUrl(),
				post.getClipSource() != null ? post.getClipSource().toApiValue() : null,
				post.getContent(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getViewCount(),
				0L,
				commentCount,
				false
		);
	}
}
