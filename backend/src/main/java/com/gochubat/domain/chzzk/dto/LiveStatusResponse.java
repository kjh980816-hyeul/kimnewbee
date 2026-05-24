package com.gochubat.domain.chzzk.dto;

public record LiveStatusResponse(
		boolean isLive,
		String title,
		int viewerCount,
		String startedAt,
		String channelUrl
) {

	public static LiveStatusResponse offline() {
		return new LiveStatusResponse(false, "", 0, null, "");
	}
}
