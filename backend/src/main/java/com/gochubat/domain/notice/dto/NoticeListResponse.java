package com.gochubat.domain.notice.dto;

import java.util.List;

public record NoticeListResponse(
		List<NoticeListItemResponse> data,
		int total
) {

	public static NoticeListResponse of(List<NoticeListItemResponse> items) {
		return new NoticeListResponse(items, items.size());
	}
}
