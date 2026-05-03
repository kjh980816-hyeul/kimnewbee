package com.gochubat.domain.user.dto;

public record UserStatsResponse(
		long postCount,
		long commentCount,
		long likeGivenCount,
		long attendanceStreak
) {

	public static UserStatsResponse zero() {
		return new UserStatsResponse(0L, 0L, 0L, 0L);
	}
}
