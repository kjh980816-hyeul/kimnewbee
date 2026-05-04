package com.gochubat.domain.cafe.dto;

import com.gochubat.domain.cafe.entity.CafeConfig;

import java.time.LocalDateTime;

public record CafeConfigResponse(
		String heroBannerUrl,
		String heroHeadline,
		String heroSubtext,
		String footerText,
		LocalDateTime updatedAt
) {

	public static CafeConfigResponse from(CafeConfig config) {
		return new CafeConfigResponse(
				config.getHeroBannerUrl(),
				config.getHeroHeadline(),
				config.getHeroSubtext(),
				config.getFooterText(),
				config.getUpdatedAt()
		);
	}
}
