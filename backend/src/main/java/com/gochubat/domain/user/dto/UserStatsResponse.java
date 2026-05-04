package com.gochubat.domain.user.dto;

public record UserStatsResponse(
		long postCount,
		long commentCount,
		long likeGivenCount,
		long attendanceStreak
) {
}
