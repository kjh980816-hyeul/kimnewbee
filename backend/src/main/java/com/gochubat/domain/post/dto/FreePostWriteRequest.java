package com.gochubat.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FreePostWriteRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank String content,
		@Size(max = 20) String category
) {

	private static final String DEFAULT_CATEGORY = "잡담";

	// 분류 미선택/빈값이면 기본 '잡담'으로 저장.
	public String categoryOrDefault() {
		return (category == null || category.isBlank()) ? DEFAULT_CATEGORY : category.trim();
	}
}
