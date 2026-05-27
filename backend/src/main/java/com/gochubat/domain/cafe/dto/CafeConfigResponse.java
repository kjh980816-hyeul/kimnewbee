package com.gochubat.domain.cafe.dto;

import com.gochubat.domain.cafe.entity.CafeConfig;

import java.time.LocalDateTime;

public record CafeConfigResponse(
		String heroBannerUrl,
		String heroBannerPosition,
		String heroHeadline,
		String heroSubtext,
		String footerText,
		String chzzkChannelId,
		LocalDateTime updatedAt
) {

	public static CafeConfigResponse from(CafeConfig config) {
		return new CafeConfigResponse(
				config.getHeroBannerUrl(),
				config.getHeroBannerPosition(),
				config.getHeroHeadline(),
				config.getHeroSubtext(),
				config.getFooterText(),
				config.getChzzkChannelId(),
				config.getUpdatedAt()
		);
	}
}
