package com.gochubat.domain.cafe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cafe_config")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CafeConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "hero_banner_url", length = 500)
	private String heroBannerUrl;

	@Column(name = "hero_headline", nullable = false, length = 80)
	private String heroHeadline;

	@Column(name = "hero_subtext", length = 200)
	private String heroSubtext;

	@Column(name = "footer_text", length = 200)
	private String footerText;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	private CafeConfig(String heroBannerUrl, String heroHeadline, String heroSubtext, String footerText) {
		this.heroBannerUrl = heroBannerUrl;
		this.heroHeadline = heroHeadline;
		this.heroSubtext = heroSubtext;
		this.footerText = footerText;
		this.updatedAt = LocalDateTime.now();
	}

	public static CafeConfig defaults() {
		return new CafeConfig(
				null,
				"고추밭에 어서오세요 🌶️",
				"김늉비 팬들이 모이는 초록고추 정원",
				"© 고추밭 — 김늉비 팬커뮤니티"
		);
	}

	public void update(String heroBannerUrl, String heroHeadline, String heroSubtext, String footerText) {
		this.heroBannerUrl = heroBannerUrl;
		this.heroHeadline = heroHeadline;
		this.heroSubtext = heroSubtext;
		this.footerText = footerText;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
