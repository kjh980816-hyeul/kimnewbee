package com.gochubat.domain.board.dto;

import com.gochubat.domain.board.entity.BoardLayout;
import com.gochubat.domain.user.entity.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BoardCreateRequest(
		@NotBlank @Size(max = 60) @Pattern(regexp = "^[a-z][a-z0-9-]{0,59}$",
				message = "slug은 소문자/숫자/하이픈만 가능합니다") String slug,
		@NotBlank @Size(max = 60) String name,
		@NotNull BoardLayout layout,
		@NotNull Tier readTier,
		@NotNull Tier writeTier
) {
}
