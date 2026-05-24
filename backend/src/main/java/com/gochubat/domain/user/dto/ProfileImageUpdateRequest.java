package com.gochubat.domain.user.dto;

import jakarta.validation.constraints.Size;

public record ProfileImageUpdateRequest(
		@Size(max = 500) String profileImage
) {
}
