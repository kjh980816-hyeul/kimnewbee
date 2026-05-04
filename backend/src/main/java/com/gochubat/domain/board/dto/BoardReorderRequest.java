package com.gochubat.domain.board.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BoardReorderRequest(@NotEmpty List<Long> orderedIds) {
}
