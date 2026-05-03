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
		if (nickname != null && !nickname.isBlank()) {
			this.nickname = nickname;
		}
		this.profileImage = profileImage;
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
