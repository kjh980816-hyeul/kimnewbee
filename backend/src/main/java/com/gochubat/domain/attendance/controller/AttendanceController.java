package com.gochubat.domain.attendance.controller;

import com.gochubat.domain.attendance.dto.AttendanceStatusResponse;
import com.gochubat.domain.attendance.dto.CheckInResponse;
import com.gochubat.domain.attendance.service.AttendanceService;
import com.gochubat.global.security.AuthenticatedController;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/checkin")
@RequiredArgsConstructor
public class AttendanceController extends AuthenticatedController {

	private final AttendanceService attendanceService;

	@GetMapping("/status")
	public AttendanceStatusResponse status(Authentication auth) {
		return attendanceService.getStatus(requireUserId(auth));
	}

	@PostMapping
	public CheckInResponse checkIn(Authentication auth) {
		return attendanceService.checkIn(requireUserId(auth));
	}
}
