package com.gochubat.global.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver")
public record NaverOauthProperties(
		String clientId,
		String clientSecret,
		String callbackUrl,
		String successRedirectUrl,
		String authorizeBaseUrl,
		String tokenBaseUrl,
		String userInfoBaseUrl
) {
}
