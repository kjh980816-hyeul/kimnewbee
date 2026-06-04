package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.DetailCounts;

import java.time.LocalDateTime;

public record BoardPostDetailResponse(
		Long id,
		String title,
		String content,
		String author,
		String authorProfileImage,
		String authorTier,
		String mediaUrl,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		long viewCount,
		long likeCount,
		long commentCount,
		boolean likedByMe
) {

	public static BoardPostDetailResponse from(Post post, DetailCounts counts) {
		return new BoardPostDetailResponse(
				post.getId(),
				post.getTitle(),
				post.getContent(),
				post.getAuthor().getNickname(),
				post.getAuthor().getProfileImage(),
				post.getAuthor().getTier().toApiValue(),
				post.getMediaUrl(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getViewCount(),
				counts.likeCount(),
				counts.commentCount(),
				counts.likedByMe()
		);
	}
}
