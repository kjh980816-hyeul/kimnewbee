package com.gochubat.domain.board.entity;

public enum BoardLayout {
	LIST,
	GALLERY,
	CARD,
	VIDEO,
	LETTER,
	RANK;

	public String toApiValue() {
		return name().toLowerCase();
	}
}
