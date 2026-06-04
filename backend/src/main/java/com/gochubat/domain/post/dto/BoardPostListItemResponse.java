package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.ListCounts;

import java.time.LocalDateTime;

public record BoardPostListItemResponse(
		Long id,
		String title,
		String author,
		String authorProfileImage,
		String mediaUrl,
		String preview,
		LocalDateTime createdAt,
		long viewCount,
		long likeCount,
		long commentCount
) {

	public static BoardPostListItemResponse from(Post post, ListCounts counts) {
		return new BoardPostListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getAuthor().getProfileImage(),
				post.getMediaUrl(),
				post.preview(),
				post.getCreatedAt(),
				post.getViewCount(),
				counts.likeCount(),
				counts.commentCount()
		);
	}
}
