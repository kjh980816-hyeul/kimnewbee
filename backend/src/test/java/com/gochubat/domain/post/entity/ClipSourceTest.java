package com.gochubat.domain.post.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClipSourceTest {

	@Test
	void detect_youtube_full_url() {
		assertThat(ClipSource.detect("https://www.youtube.com/watch?v=abc")).isEqualTo(ClipSource.YOUTUBE);
	}

	@Test
	void detect_youtube_short_url() {
		assertThat(ClipSource.detect("https://youtu.be/abc")).isEqualTo(ClipSource.YOUTUBE);
	}

	@Test
	void detect_chzzk_url() {
		assertThat(ClipSource.detect("https://chzzk.naver.com/clips/xyz")).isEqualTo(ClipSource.CHZZK);
	}

	@Test
	void detect_other_url() {
		assertThat(ClipSource.detect("https://vimeo.com/123")).isEqualTo(ClipSource.OTHER);
	}

	@Test
	void detect_null_returns_other() {
		assertThat(ClipSource.detect(null)).isEqualTo(ClipSource.OTHER);
	}

	@Test
	void to_api_value_lowercases() {
		assertThat(ClipSource.YOUTUBE.toApiValue()).isEqualTo("youtube");
		assertThat(ClipSource.CHZZK.toApiValue()).isEqualTo("chzzk");
	}
}
