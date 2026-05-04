package com.gochubat.domain.admin.dto;

public record AdminDashboardResponse(
		long totalUsers,
		long totalPosts,
		long totalComments,
		long totalLikes
) {
}
