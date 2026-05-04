package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.ListCounts;

import java.time.LocalDateTime;

public record PetListItemResponse(
		Long id,
		String title,
		String author,
		String thumbnailUrl,
		LocalDateTime createdAt,
		long likeCount,
		long commentCount
) {

	public static PetListItemResponse from(Post post, ListCounts counts) {
		return new PetListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getMediaUrl(),
				post.getCreatedAt(),
				counts.likeCount(),
				counts.commentCount()
		);
	}
}
