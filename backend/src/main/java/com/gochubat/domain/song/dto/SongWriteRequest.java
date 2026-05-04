package com.gochubat.domain.song.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SongWriteRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank @Size(max = 200) String artist,
		@NotBlank @Size(max = 500) String link
) {
}
