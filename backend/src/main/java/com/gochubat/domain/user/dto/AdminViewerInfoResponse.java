package com.gochubat.domain.user.dto;

public record AdminViewerInfoResponse(boolean isAdmin) {

	public static AdminViewerInfoResponse of(boolean isAdmin) {
		return new AdminViewerInfoResponse(isAdmin);
	}
}
