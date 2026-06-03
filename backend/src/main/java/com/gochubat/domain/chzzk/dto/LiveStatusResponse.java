package com.gochubat.domain.chzzk.dto;

public record LiveStatusResponse(
		boolean isLive,
		String title,
		int viewerCount,
		String startedAt,
		String channelUrl,
		String thumbnailUrl
) {

	public static LiveStatusResponse offline() {
		return new LiveStatusResponse(false, "", 0, null, "", null);
	}
}
