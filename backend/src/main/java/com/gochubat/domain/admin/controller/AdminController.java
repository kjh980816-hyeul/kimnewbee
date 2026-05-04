package com.gochubat.domain.admin.controller;

import com.gochubat.domain.admin.dto.AdjustPointsRequest;
import com.gochubat.domain.admin.dto.AdminDashboardResponse;
import com.gochubat.domain.admin.dto.AdminUserResponse;
import com.gochubat.domain.admin.dto.ChangeTierRequest;
import com.gochubat.domain.admin.service.AdminService;
import com.gochubat.global.dto.ListResponse;
import com.gochubat.global.security.AuthenticatedController;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('OWNER')")
public class AdminController extends AuthenticatedController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping("/dashboard")
	public AdminDashboardResponse dashboard() {
		return adminService.dashboard();
	}

	@GetMapping("/users")
	public ListResponse<AdminUserResponse> users() {
		return ListResponse.of(adminService.listUsers());
	}

	@PatchMapping("/users/{id}/tier")
	public AdminUserResponse changeTier(@PathVariable Long id, @Valid @RequestBody ChangeTierRequest request) {
		return adminService.changeTier(id, request.tier());
	}

	@PatchMapping("/users/{id}/points")
	public AdminUserResponse adjustPoints(@PathVariable Long id, @Valid @RequestBody AdjustPointsRequest request) {
		return adminService.adjustPoints(id, request.delta());
	}
}
