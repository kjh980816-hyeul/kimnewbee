package com.gochubat.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClipWriteRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank @Size(max = 500) String videoUrl,
		@NotBlank String description
) {
}
