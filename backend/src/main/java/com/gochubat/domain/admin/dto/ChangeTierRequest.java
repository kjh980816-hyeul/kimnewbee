package com.gochubat.domain.admin.dto;

import com.gochubat.domain.user.entity.Tier;
import jakarta.validation.constraints.NotNull;

public record ChangeTierRequest(@NotNull Tier tier) {
}
