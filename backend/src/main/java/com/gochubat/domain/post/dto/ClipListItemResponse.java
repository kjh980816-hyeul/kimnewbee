package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.ListCounts;

import java.time.LocalDateTime;

public record ClipListItemResponse(
		Long id,
		String title,
		String author,
		String videoUrl,
		String source,
		LocalDateTime createdAt,
		long likeCount,
		long commentCount
) {

	public static ClipListItemResponse from(Post post, ListCounts counts) {
		return new ClipListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getAuthor().getNickname(),
				post.getMediaUrl(),
				post.getClipSource() != null ? post.getClipSource().toApiValue() : null,
				post.getCreatedAt(),
				counts.likeCount(),
				counts.commentCount()
		);
	}
}
