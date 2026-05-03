package com.gochubat.domain.post.entity;

public enum ClipSource {
	YOUTUBE,
	CHZZK,
	OTHER;

	public static ClipSource detect(String url) {
		if (url == null) {
			return OTHER;
		}
		String lowered = url.toLowerCase();
		if (lowered.contains("youtube.com") || lowered.contains("youtu.be")) {
			return YOUTUBE;
		}
		if (lowered.contains("chzzk.naver.com")) {
			return CHZZK;
		}
		return OTHER;
	}

	public String toApiValue() {
		return name().toLowerCase();
	}
}
