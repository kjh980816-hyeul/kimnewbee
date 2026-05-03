package com.gochubat.global.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverUserInfoResponse(
		String resultcode,
		String message,
		Profile response
) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Profile(
			String id,
			String nickname,
			String name,
			String profile_image
	) {
	}
}
