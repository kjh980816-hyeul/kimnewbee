package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.DetailCounts;

import java.time.LocalDateTime;

public record PetDetailResponse(
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

	public static PetDetailResponse from(Post post, DetailCounts counts) {
		return new PetDetailResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getMediaUrl(),
				post.getMediaUrl(),
				post.getContent(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getViewCount(),
				counts.likeCount(),
				counts.commentCount(),
				counts.likedByMe()
		);
	}
}
