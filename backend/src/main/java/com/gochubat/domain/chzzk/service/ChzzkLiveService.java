package com.gochubat.domain.chzzk.service;

import com.gochubat.domain.cafe.service.CafeConfigService;
import com.gochubat.domain.chzzk.dto.ChzzkLiveDetail;
import com.gochubat.domain.chzzk.dto.LiveStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChzzkLiveService {

	private static final Logger log = LoggerFactory.getLogger(ChzzkLiveService.class);
	// 치지직이 service/v1 live-detail 공개 접근을 차단(code 9004)해서 polling live-status로 전환. 응답 구조는 동일.
	private static final String LIVE_STATUS_URL = "https://api.chzzk.naver.com/polling/v3/channels/{channelId}/live-status";
	private static final String CHANNEL_URL = "https://chzzk.naver.com/live/";
	private static final Duration CACHE_TTL = Duration.ofSeconds(30);
	private static final DateTimeFormatter OPEN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final CafeConfigService cafeConfigService;
	private final RestClient restClient;
	private final AtomicReference<Cached> cache = new AtomicReference<>();

	public ChzzkLiveService(CafeConfigService cafeConfigService, RestClient restClient) {
		this.cafeConfigService = cafeConfigService;
		this.restClient = restClient;
	}

	public LiveStatusResponse currentStatus() {
		String channelId = cafeConfigService.currentChzzkChannelId();
		if (channelId == null || channelId.isBlank()) {
			return LiveStatusResponse.offline();
		}
		Cached snapshot = cache.get();
		if (snapshot != null && snapshot.channelId.equals(channelId) && !snapshot.isExpired()) {
			return snapshot.response;
		}
		LiveStatusResponse fresh = fetchFromChzzk(channelId);
		cache.set(new Cached(channelId, fresh, System.currentTimeMillis()));
		return fresh;
	}

	private LiveStatusResponse fetchFromChzzk(String channelId) {
		try {
			ChzzkLiveDetail detail = restClient.get()
					.uri(LIVE_STATUS_URL, channelId)
					.retrieve()
					.body(ChzzkLiveDetail.class);

			if (detail == null || detail.content() == null) {
				return LiveStatusResponse.offline();
			}
			ChzzkLiveDetail.Content c = detail.content();
			boolean isLive = "OPEN".equalsIgnoreCase(c.status());
			if (!isLive) {
				return new LiveStatusResponse(false, "", 0, null, CHANNEL_URL + channelId);
			}
			return new LiveStatusResponse(
					true,
					c.liveTitle() == null ? "" : c.liveTitle(),
					c.concurrentUserCount(),
					parseOpenDate(c.openDate()),
					CHANNEL_URL + channelId
			);
		} catch (Exception e) {
			log.warn("Failed to fetch chzzk live-status for channel {}: {}", channelId, e.getMessage());
			return LiveStatusResponse.offline();
		}
	}

	private String parseOpenDate(String openDate) {
		if (openDate == null || openDate.isBlank()) return null;
		try {
			LocalDateTime ldt = LocalDateTime.parse(openDate, OPEN_DATE_FORMAT);
			return ldt.atZone(KST).toOffsetDateTime().toString();
		} catch (Exception e) {
			return null;
		}
	}

	private record Cached(String channelId, LiveStatusResponse response, long timestamp) {
		boolean isExpired() {
			return System.currentTimeMillis() - timestamp > CACHE_TTL.toMillis();
		}
	}
}
