package com.gochubat.domain.board.dto;

import com.gochubat.domain.board.entity.BoardLayout;
import com.gochubat.domain.user.entity.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoardUpdateRequest(
		@NotBlank @Size(max = 60) String name,
		@NotNull BoardLayout layout,
		@NotNull Tier readTier,
		@NotNull Tier writeTier,
		@NotNull Boolean active
) {
}
