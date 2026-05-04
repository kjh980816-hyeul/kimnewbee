package com.gochubat.domain.song.entity;

import com.gochubat.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Song {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, length = 200)
	private String artist;

	@Column(nullable = false, length = 500)
	private String link;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "submitted_by", nullable = false)
	private User submittedBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private Song(String title, String artist, String link, User submittedBy) {
		this.title = title;
		this.artist = artist;
		this.link = link;
		this.submittedBy = submittedBy;
	}

	public static Song create(String title, String artist, String link, User submittedBy) {
		return new Song(title, artist, link, submittedBy);
	}

	@PrePersist
	void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
