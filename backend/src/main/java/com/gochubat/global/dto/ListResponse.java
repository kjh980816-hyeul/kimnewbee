package com.gochubat.global.dto;

import java.util.List;

public record ListResponse<T>(List<T> data, int total) {

	public static <T> ListResponse<T> of(List<T> data) {
		return new ListResponse<>(data, data.size());
	}
}
