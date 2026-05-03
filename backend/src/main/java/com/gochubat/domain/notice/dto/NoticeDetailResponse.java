package com.gochubat.domain.notice.dto;

import com.gochubat.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
		Long id,
		String title,
		String content,
		String author,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		long viewCount
) {

	public static NoticeDetailResponse from(Notice notice) {
		return new NoticeDetailResponse(
				notice.getId(),
				notice.getTitle(),
				notice.getContent(),
				notice.getAuthor().getNickname(),
				notice.getCreatedAt(),
				notice.getUpdatedAt(),
				notice.getViewCount()
		);
	}
}
