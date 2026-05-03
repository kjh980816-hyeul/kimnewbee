package com.gochubat.domain.notice.dto;

import com.gochubat.domain.notice.entity.Notice;

import java.time.LocalDateTime;

public record NoticeListItemResponse(
		Long id,
		String title,
		String author,
		LocalDateTime createdAt,
		long viewCount
) {

	public static NoticeListItemResponse from(Notice notice) {
		return new NoticeListItemResponse(
				notice.getId(),
				notice.getTitle(),
				notice.getAuthor().getNickname(),
				notice.getCreatedAt(),
				notice.getViewCount()
		);
	}
}
