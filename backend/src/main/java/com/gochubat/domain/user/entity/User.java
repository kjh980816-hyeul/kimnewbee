package com.gochubat.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "naver_id", nullable = false, unique = true, length = 64)
	private String naverId;

	@Column(nullable = false, length = 30)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private Tier tier;

	@Column(nullable = false)
	private long points;

	@Column(name = "profile_image_url", length = 500)
	private String profileImage;

	@Column(name = "nickname_changed_at")
	private LocalDateTime nicknameChangedAt;

	@Column(name = "profile_image_changed_at")
	private LocalDateTime profileImageChangedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private User(String naverId, String nickname, String profileImage) {
		this.naverId = naverId;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.tier = Tier.SEED;
		this.points = 0L;
	}

	public static User createFromNaver(String naverId, String nickname, String profileImage) {
		return new User(naverId, nickname, profileImage);
	}

	public void syncFromNaver(String nickname, String profileImage) {
		if (nickname != null && !nickname.isBlank() && this.nicknameChangedAt == null) {
			this.nickname = nickname;
		}
		if (this.profileImageChangedAt == null) {
			this.profileImage = profileImage;
		}
	}

	public void changeNickname(String nickname, LocalDateTime now) {
		this.nickname = nickname;
		this.nicknameChangedAt = now;
	}

	public void addPoints(long delta) {
		this.points = Math.max(0L, this.points + delta);
	}

	public void promoteTo(Tier next) {
		if (next.ordinal() > this.tier.ordinal()) {
			this.tier = next;
		}
	}

	public void changeTier(Tier next) {
		this.tier = next;
	}

	public void changeProfileImage(String url, LocalDateTime now) {
		this.profileImage = (url == null || url.isBlank()) ? null : url;
		this.profileImageChangedAt = now;
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
