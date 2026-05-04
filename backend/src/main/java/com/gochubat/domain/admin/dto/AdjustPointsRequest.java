package com.gochubat.domain.admin.dto;

import jakarta.validation.constraints.NotNull;

public record AdjustPointsRequest(@NotNull Long delta) {
}
