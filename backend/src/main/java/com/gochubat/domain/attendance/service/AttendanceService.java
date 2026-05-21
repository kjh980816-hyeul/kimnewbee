package com.gochubat.domain.attendance.service;

import com.gochubat.domain.attendance.dto.AttendanceStatusResponse;
import com.gochubat.domain.attendance.dto.CheckInResponse;
import com.gochubat.domain.attendance.entity.Attendance;
import com.gochubat.domain.attendance.repository.AttendanceRepository;
import com.gochubat.domain.point.PointPolicy;
import com.gochubat.domain.point.PointReason;
import com.gochubat.domain.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;
	private final PointService pointService;
	private final PointPolicy pointPolicy;

	@Transactional(readOnly = true)
	public AttendanceStatusResponse getStatus(Long userId) {
		LocalDate today = LocalDate.now();
		LocalDate firstOfMonth = today.withDayOfMonth(1);
		LocalDate lastOfMonth = today.withDayOfMonth(today.lengthOfMonth());
		List<LocalDate> monthDates = attendanceRepository.findDatesInRange(userId, firstOfMonth, lastOfMonth);
		int currentStreak = currentStreakAsOf(userId, today);
		boolean checkedInToday = attendanceRepository.findByUserIdAndDate(userId, today).isPresent();
		long awarded = checkedInToday ? pointPolicy.attendance() : 0L;
		return new AttendanceStatusResponse(checkedInToday, currentStreak, awarded, monthDates, today);
	}

	@Transactional
	public CheckInResponse checkIn(Long userId) {
		LocalDate today = LocalDate.now();
		return attendanceRepository.findByUserIdAndDate(userId, today)
				.map(existing -> new CheckInResponse(true, today, existing.getStreak(), 0L))
				.orElseGet(() -> doCheckIn(userId, today));
	}

	private CheckInResponse doCheckIn(Long userId, LocalDate today) {
		int previousStreak = attendanceRepository.findByUserIdAndDate(userId, today.minusDays(1))
				.map(Attendance::getStreak)
				.orElse(0);
		int nextStreak = previousStreak + 1;
		attendanceRepository.save(Attendance.create(userId, today, nextStreak));
		pointService.award(userId, PointReason.ATTENDANCE);
		return new CheckInResponse(false, today, nextStreak, pointPolicy.attendance());
	}

	private int currentStreakAsOf(Long userId, LocalDate today) {
		return attendanceRepository.findByUserIdAndDate(userId, today)
				.or(() -> attendanceRepository.findByUserIdAndDate(userId, today.minusDays(1)))
				.map(Attendance::getStreak)
				.orElse(0);
	}
}
