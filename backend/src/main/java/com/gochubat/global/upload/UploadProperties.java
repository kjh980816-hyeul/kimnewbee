package com.gochubat.global.upload;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "gochubat.upload")
public record UploadProperties(
		String dir,
		long maxSizeBytes,
		Set<String> allowedMimeTypes,
		String publicBaseUrl
) {
}
