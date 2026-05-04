package com.gochubat.domain.letter.dto;

import jakarta.validation.constraints.NotBlank;

public record LetterWriteRequest(
		@NotBlank String content
) {
}
