package com.gochubat.domain.letter.dto;

import com.gochubat.domain.letter.entity.Letter;

import java.time.LocalDateTime;

public record LetterDetailResponse(
		Long id,
		String author,
		String preview,
		String content,
		boolean isReadByAdmin,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {

	public static LetterDetailResponse from(Letter letter) {
		return new LetterDetailResponse(
				letter.getId(),
				letter.getAuthor().getNickname(),
				letter.preview(),
				letter.getContent(),
				letter.isReadByAdmin(),
				letter.getCreatedAt(),
				letter.getUpdatedAt()
		);
	}
}
