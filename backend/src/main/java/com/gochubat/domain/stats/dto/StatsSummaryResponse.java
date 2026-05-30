package com.gochubat.domain.stats.dto;

public record StatsSummaryResponse(
		long totalMembers,
		long todayNewPosts
) {
}
