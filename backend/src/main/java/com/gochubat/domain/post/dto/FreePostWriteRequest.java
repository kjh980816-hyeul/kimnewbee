package com.gochubat.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FreePostWriteRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank String content
) {
}
