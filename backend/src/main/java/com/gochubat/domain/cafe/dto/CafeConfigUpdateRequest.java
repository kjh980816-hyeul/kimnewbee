package com.gochubat.domain.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CafeConfigUpdateRequest(
		@Size(max = 500) String heroBannerUrl,
		@NotBlank @Size(max = 80) String heroHeadline,
		@Size(max = 200) String heroSubtext,
		@Size(max = 200) String footerText,
		@Size(max = 64) @Pattern(regexp = "^$|^[a-f0-9]{32}$",
				message = "치지직 채널 ID는 32자 16진수여야 합니다") String chzzkChannelId
) {
}
