package com.gochubat.domain.chzzk.controller;

import com.gochubat.domain.chzzk.dto.LiveStatusResponse;
import com.gochubat.domain.chzzk.service.ChzzkLiveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chzzk")
public class ChzzkLiveController {

	private final ChzzkLiveService service;

	public ChzzkLiveController(ChzzkLiveService service) {
		this.service = service;
	}

	@GetMapping("/live")
	public LiveStatusResponse live() {
		return service.currentStatus();
	}
}
