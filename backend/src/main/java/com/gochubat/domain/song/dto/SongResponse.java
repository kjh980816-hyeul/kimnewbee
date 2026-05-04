package com.gochubat.domain.song.dto;

import com.gochubat.domain.song.entity.Song;

import java.time.LocalDateTime;

public record SongResponse(
		Long id,
		String title,
		String artist,
		String link,
		String submittedBy,
		long voteCount,
		boolean votedByMe,
		LocalDateTime createdAt
) {

	public static SongResponse from(Song song, long voteCount, boolean votedByMe) {
		return new SongResponse(
				song.getId(),
				song.getTitle(),
				song.getArtist(),
				song.getLink(),
				song.getSubmittedBy().getNickname(),
				voteCount,
				votedByMe,
				song.getCreatedAt()
		);
	}
}
