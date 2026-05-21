package com.gochubat.domain.attendance.dto;

import java.time.LocalDate;

public record CheckInResponse(
		boolean alreadyCheckedIn,
		LocalDate date,
		int currentStreak,
		long pointsAwarded
) {}
