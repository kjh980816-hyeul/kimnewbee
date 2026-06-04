package com.gochubat.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardPostWriteRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank String content,
		@Size(max = 500) String mediaUrl
) {

	// 갤러리/영상형 레이아웃에서 쓰는 이미지/영상 URL. 비었으면 null로 저장.
	public String mediaUrlOrNull() {
		return (mediaUrl == null || mediaUrl.isBlank()) ? null : mediaUrl.trim();
	}
}
