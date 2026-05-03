package com.gochubat.domain.post.entity;

import com.gochubat.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "board_type", nullable = false, length = 16)
	private BoardType type;

	@Column(nullable = false, length = 200)
	private String title;

	@Lob
	@Column(nullable = false)
	private String content;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Column(name = "view_count", nullable = false)
	private long viewCount;

	@Column(name = "media_url", length = 500)
	private String mediaUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "clip_source", length = 16)
	private ClipSource clipSource;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	private Post(BoardType type, String title, String content, User author, String mediaUrl, ClipSource clipSource) {
		this.type = type;
		this.title = title;
		this.content = content;
		this.author = author;
		this.mediaUrl = mediaUrl;
		this.clipSource = clipSource;
		this.viewCount = 0L;
	}

	public static Post createFree(String title, String content, User author) {
		return new Post(BoardType.FREE, title, content, author, null, null);
	}

	public static Post createFanart(String title, String content, User author, String imageUrl) {
		return new Post(BoardType.FANART, title, content, author, imageUrl, null);
	}

	public static Post createClip(String title, String description, User author, String videoUrl) {
		return new Post(BoardType.CLIP, title, description, author, videoUrl, ClipSource.detect(videoUrl));
	}

	public void incrementViewCount() {
		this.viewCount++;
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
