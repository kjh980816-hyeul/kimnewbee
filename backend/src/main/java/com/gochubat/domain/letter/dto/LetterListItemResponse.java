package com.gochubat.domain.letter.dto;

import com.gochubat.domain.letter.entity.Letter;

import java.time.LocalDateTime;

public record LetterListItemResponse(
		Long id,
		String author,
		String preview,
		LocalDateTime createdAt
) {

	public static LetterListItemResponse from(Letter letter) {
		return new LetterListItemResponse(
				letter.getId(),
				letter.getAuthor().getNickname(),
				letter.preview(),
				letter.getCreatedAt()
		);
	}
}
