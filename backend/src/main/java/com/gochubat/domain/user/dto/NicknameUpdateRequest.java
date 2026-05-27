package com.gochubat.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
		@NotBlank
		@Size(min = 2, max = 20)
		@Pattern(regexp = "^[\\p{L}\\p{N} _-]+$", message = "닉네임은 한글/영문/숫자/공백/_-만 사용할 수 있어요")
		String nickname
) {
}
