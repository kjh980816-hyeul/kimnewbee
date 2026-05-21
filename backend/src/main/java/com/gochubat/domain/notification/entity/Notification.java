package com.gochubat.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
		@Index(name = "idx_notifications_recipient", columnList = "recipient_id,is_read,created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipient_id", nullable = false)
	private Long recipientId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private NotificationType type;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, length = 300)
	private String message;

	@Column(length = 200)
	private String link;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private Notification(Long recipientId, NotificationType type, String title, String message, String link) {
		this.recipientId = recipientId;
		this.type = type;
		this.title = title;
		this.message = message;
		this.link = link;
		this.read = false;
	}

	public static Notification create(Long recipientId, NotificationType type, String title, String message, String link) {
		return new Notification(recipientId, type, title, message, link);
	}

	public void markRead() {
		this.read = true;
	}

	public boolean isOwnedBy(Long userId) {
		return recipientId.equals(userId);
	}

	@PrePersist
	void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}
