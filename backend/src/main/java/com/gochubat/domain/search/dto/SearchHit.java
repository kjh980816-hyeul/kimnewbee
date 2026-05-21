package com.gochubat.domain.search.dto;

import java.time.LocalDateTime;

public record SearchHit(
		String type,
		Long id,
		String title,
		String snippet,
		String boardSlug,
		String boardLabel,
		String author,
		LocalDateTime createdAt
) {}
