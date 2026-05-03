package com.gochubat.domain.user.entity;

public enum Tier {
	SEED,
	PEPPER,
	CORN,
	OWNER;

	public String toApiValue() {
		return name().toLowerCase();
	}
}
