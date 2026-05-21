package com.gochubat.domain.search.dto;

import java.util.List;

public record SearchResponse(
		String query,
		int total,
		List<SearchHit> hits
) {}
