package com.gochubat.domain.letter.entity;

import com.gochubat.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "letters")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Letter {

	private static final int PREVIEW_MAX_LENGTH = 30;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Lob
	@Column(nullable = false)
	private String content;

	@Column(name = "is_read_by_admin", nullable = false)
	private boolean readByAdmin;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	private Letter(User author, String content) {
		this.author = author;
		this.content = content;
		this.readByAdmin = false;
	}

	public static Letter create(User author, String content) {
		return new Letter(author, content);
	}

	public void markReadByAdmin() {
		if (!this.readByAdmin) {
			this.readByAdmin = true;
		}
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
