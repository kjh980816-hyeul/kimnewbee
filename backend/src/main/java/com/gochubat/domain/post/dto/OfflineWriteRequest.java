package com.gochubat.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OfflineWriteRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank @Size(max = 200) String location,
		@NotNull LocalDate meetupDate,
		@NotBlank @Size(max = 500) String imageUrl,
		@NotBlank String content
) {
}
