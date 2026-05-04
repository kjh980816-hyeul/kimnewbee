package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.ListCounts;

import java.time.LocalDateTime;

public record FreePostListItemResponse(
		Long id,
		String title,
		String author,
		LocalDateTime createdAt,
		long viewCount,
		long likeCount,
		long commentCount
) {

	public static FreePostListItemResponse from(Post post, ListCounts counts) {
		return new FreePostListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getCreatedAt(),
				post.getViewCount(),
				counts.likeCount(),
				counts.commentCount()
		);
	}
}
