package com.gochubat.global.exception;

import java.time.Instant;

public record ErrorResponse(String code, String message, Instant timestamp) {

	public static ErrorResponse of(ErrorCode errorCode) {
		return new ErrorResponse(errorCode.code(), errorCode.message(), Instant.now());
	}

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.code(), message, Instant.now());
	}
}
