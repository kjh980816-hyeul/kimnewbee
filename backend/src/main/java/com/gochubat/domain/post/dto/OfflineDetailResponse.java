package com.gochubat.domain.post.dto;

import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.service.PostBoardAssembler.DetailCounts;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OfflineDetailResponse(
		Long id,
		String title,
		String location,
		LocalDate meetupDate,
		String thumbnailUrl,
		String imageUrl,
		String content,
		String preview,
		String author,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		long viewCount,
		long likeCount,
		long commentCount,
		boolean likedByMe
) {

	public static OfflineDetailResponse from(Post post, DetailCounts counts) {
		return new OfflineDetailResponse(
				post.getId(),
				post.getTitle(),
				post.getLocation(),
				post.getMeetupDate(),
				post.getMediaUrl(),
				post.getMediaUrl(),
				post.getContent(),
				post.preview(),
				post.getAuthor().getNickname(),
				post.getCreatedAt(),
				post.getUpdatedAt(),
				post.getViewCount(),
				counts.likeCount(),
				counts.commentCount(),
				counts.likedByMe()
		);
	}
}
