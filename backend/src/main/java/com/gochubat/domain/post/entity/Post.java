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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

	private static final int PREVIEW_MAX_LENGTH = 60;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "board_type", nullable = false, length = 16)
	private BoardType type;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
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

	@Column(name = "location", length = 200)
	private String location;

	@Column(name = "meetup_date")
	private LocalDate meetupDate;

	// 자유게시판 분류(잡담/질문/후기/정보). free 외 게시판은 null.
	@Column(name = "category", length = 20)
	private String category;

	// 관리자가 만든 커스텀 게시판의 slug. 기본 8개 게시판(FREE 등)은 null.
	@Column(name = "board_slug", length = 60)
	private String boardSlug;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	private Post(
			BoardType type,
			String title,
			String content,
			User author,
			String mediaUrl,
			ClipSource clipSource,
			String location,
			LocalDate meetupDate,
			String category,
			String boardSlug
	) {
		this.type = type;
		this.title = title;
		this.content = content;
		this.author = author;
		this.mediaUrl = mediaUrl;
		this.clipSource = clipSource;
		this.location = location;
		this.meetupDate = meetupDate;
		this.category = category;
		this.boardSlug = boardSlug;
		this.viewCount = 0L;
	}

	public static Post createFree(String title, String content, User author) {
		return createFree(title, content, author, "잡담");
	}

	public static Post createFree(String title, String content, User author, String category) {
		return new Post(BoardType.FREE, title, content, author, null, null, null, null, category, null);
	}

	public static Post createFanart(String title, String content, User author, String imageUrl) {
		return new Post(BoardType.FANART, title, content, author, imageUrl, null, null, null, null, null);
	}

	public static Post createClip(String title, String description, User author, String videoUrl) {
		return new Post(BoardType.CLIP, title, description, author, videoUrl, ClipSource.detect(videoUrl), null, null, null, null);
	}

	public static Post createPet(String title, String content, User author, String imageUrl) {
		return new Post(BoardType.PET, title, content, author, imageUrl, null, null, null, null, null);
	}

	public static Post createOffline(
			String title,
			String content,
			User author,
			String imageUrl,
			String location,
			LocalDate meetupDate
	) {
		return new Post(BoardType.OFFLINE, title, content, author, imageUrl, null, location, meetupDate, null, null);
	}

	// 관리자가 만든 커스텀 게시판 글. mediaUrl은 갤러리/영상형 레이아웃용(선택).
	public static Post createCustom(String boardSlug, String title, String content, User author, String mediaUrl) {
		return new Post(BoardType.CUSTOM, title, content, author, mediaUrl, null, null, null, null, boardSlug);
	}

	public void incrementViewCount() {
		this.viewCount++;
	}

	public String preview() {
		if (content.length() <= PREVIEW_MAX_LENGTH) {
			return content;
		}
		return content.substring(0, PREVIEW_MAX_LENGTH) + "...";
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
