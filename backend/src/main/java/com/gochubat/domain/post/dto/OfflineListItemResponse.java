package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.ListCounts;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OfflineListItemResponse(
		Long id,
		String title,
		String location,
		LocalDate meetupDate,
		String thumbnailUrl,
		String preview,
		String author,
		LocalDateTime createdAt,
		long likeCount,
		long commentCount
) {

	public static OfflineListItemResponse from(Post post, ListCounts counts) {
		return new OfflineListItemResponse(
				post.getId(),
				post.getTitle(),
				post.getLocation(),
				post.getMeetupDate(),
				post.getMediaUrl(),
				post.preview(),
				post.getAuthor().getNickname(),
				post.getCreatedAt(),
				counts.likeCount(),
				counts.commentCount()
		);
	}
}
