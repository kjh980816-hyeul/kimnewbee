package com.gochubat.global.oauth;

import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class NaverOauthClient {

	private static final Logger log = LoggerFactory.getLogger(NaverOauthClient.class);
	private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
	private static final SecureRandom RANDOM = new SecureRandom();

	private final NaverOauthProperties properties;
	private final RestClient restClient;

	public NaverOauthClient(NaverOauthProperties properties, RestClient restClient) {
		this.properties = properties;
		this.restClient = restClient;
	}

	public String generateState() {
		byte[] bytes = new byte[24];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public URI buildAuthorizeUri(String state) {
		return UriComponentsBuilder.fromUriString(properties.authorizeBaseUrl())
				.queryParam("response_type", "code")
				.queryParam("client_id", properties.clientId())
				.queryParam("redirect_uri", properties.callbackUrl())
				.queryParam("state", state)
				.build(true)
				.toUri();
	}

	public NaverTokenResponse exchangeCodeForToken(String code, String state) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", GRANT_TYPE_AUTHORIZATION_CODE);
		form.add("client_id", properties.clientId());
		form.add("client_secret", properties.clientSecret());
		form.add("code", code);
		form.add("state", state);

		NaverTokenResponse response = restClient.post()
				.uri(properties.tokenBaseUrl())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.acceptCharset(StandardCharsets.UTF_8)
				.body(form)
				.retrieve()
				.body(NaverTokenResponse.class);

		if (response == null || response.accessToken() == null) {
			log.warn("Naver token exchange failed: {}", response);
			throw new CustomException(ErrorCode.OAUTH_FAILED);
		}
		return response;
	}

	public NaverUserInfoResponse fetchUserInfo(String accessToken) {
		NaverUserInfoResponse response = restClient.get()
				.uri(properties.userInfoBaseUrl())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.body(NaverUserInfoResponse.class);

		if (response == null || response.response() == null || response.response().id() == null) {
			log.warn("Naver user info fetch failed: {}", response);
			throw new CustomException(ErrorCode.OAUTH_FAILED);
		}
		return response;
	}
}
