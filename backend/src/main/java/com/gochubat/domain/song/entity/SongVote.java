package com.gochubat.domain.song.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "song_votes",
		uniqueConstraints = @UniqueConstraint(name = "uk_song_votes_song_user", columnNames = {"song_id", "user_id"}),
		indexes = {
				@Index(name = "idx_song_votes_song_id", columnList = "song_id"),
				@Index(name = "idx_song_votes_user_id", columnList = "user_id")
		})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SongVote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "song_id", nullable = false)
	private Long songId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	private SongVote(Long songId, Long userId) {
		this.songId = songId;
		this.userId = userId;
	}

	public static SongVote create(Long songId, Long userId) {
		return new SongVote(songId, userId);
	}
}
