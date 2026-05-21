package com.gochubat.domain.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public record AttendanceStatusResponse(
		boolean checkedInToday,
		int currentStreak,
		long pointsAwardedToday,
		List<LocalDate> monthDates,
		LocalDate today
) {}
