package com.gochubat.global.security;

import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;

public abstract class AuthenticatedController {

	protected final Long requireUserId(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
		return userId;
	}

	protected final Long currentUserIdOrNull(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
			return null;
		}
		return userId;
	}
}
