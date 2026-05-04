package com.gochubat.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentWriteRequest(
		Long parentId,
		@NotBlank @Size(max = 2000) String content
) {
}
