package com.gochubat.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeWriteRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank String content
) {
}
