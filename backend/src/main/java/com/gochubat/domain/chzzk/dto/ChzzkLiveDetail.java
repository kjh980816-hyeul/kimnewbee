package com.gochubat.domain.chzzk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChzzkLiveDetail(int code, Content content) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Content(
			String liveTitle,
			String status,
			int concurrentUserCount,
			String openDate
	) {
	}
}
