package com.gochubat.domain.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CafeConfigUpdateRequest(
		@Size(max = 500) String heroBannerUrl,
		@NotBlank @Size(max = 80) String heroHeadline,
		@Size(max = 200) String heroSubtext,
		@Size(max = 200) String footerText
) {
}
