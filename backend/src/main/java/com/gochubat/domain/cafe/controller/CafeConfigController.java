package com.gochubat.domain.cafe.controller;

import com.gochubat.domain.cafe.dto.CafeConfigResponse;
import com.gochubat.domain.cafe.dto.CafeConfigUpdateRequest;
import com.gochubat.domain.cafe.service.CafeConfigService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CafeConfigController {

	private final CafeConfigService service;

	public CafeConfigController(CafeConfigService service) {
		this.service = service;
	}

	@GetMapping("/api/cafe/config")
	public CafeConfigResponse publicGet() {
		return service.get();
	}

	@PutMapping("/api/admin/cafe/config")
	@PreAuthorize("hasRole('OWNER')")
	public CafeConfigResponse adminUpdate(@Valid @RequestBody CafeConfigUpdateRequest request) {
		return service.update(request);
	}
}
