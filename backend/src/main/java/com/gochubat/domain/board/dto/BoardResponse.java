package com.gochubat.domain.board.dto;

import com.gochubat.domain.board.entity.Board;

public record BoardResponse(
		Long id,
		String slug,
		String name,
		String layout,
		String readTier,
		String writeTier,
		int orderIndex,
		boolean active
) {

	public static BoardResponse from(Board board) {
		return new BoardResponse(
				board.getId(),
				board.getSlug(),
				board.getName(),
				board.getLayout().toApiValue(),
				board.getReadTier().toApiValue(),
				board.getWriteTier().toApiValue(),
				board.getOrderIndex(),
				board.isActive()
		);
	}
}
