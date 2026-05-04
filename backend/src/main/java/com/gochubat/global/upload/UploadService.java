package com.gochubat.global.upload;

import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadService {

	private static final Logger log = LoggerFactory.getLogger(UploadService.class);

	private final UploadProperties properties;
	private final Path baseDir;

	public UploadService(UploadProperties properties) {
		this.properties = properties;
		this.baseDir = Paths.get(properties.dir()).toAbsolutePath().normalize();
	}

	@PostConstruct
	void ensureBaseDir() throws IOException {
		Files.createDirectories(baseDir);
		log.info("Upload base dir: {}", baseDir);
	}

	public String store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
		if (file.getSize() > properties.maxSizeBytes()) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
		String contentType = file.getContentType();
		if (contentType == null || !properties.allowedMimeTypes().contains(contentType)) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		String extension = extensionFor(contentType);
		String filename = UUID.randomUUID() + extension;
		Path destination = baseDir.resolve(filename).normalize();
		if (!destination.startsWith(baseDir)) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}

		try {
			file.transferTo(destination);
		} catch (IOException e) {
			log.error("File save failed", e);
			throw new CustomException(ErrorCode.INTERNAL_ERROR);
		}

		String publicBase = properties.publicBaseUrl().endsWith("/")
				? properties.publicBaseUrl().substring(0, properties.publicBaseUrl().length() - 1)
				: properties.publicBaseUrl();
		return publicBase + "/" + filename;
	}

	private String extensionFor(String contentType) {
		return switch (contentType) {
			case "image/jpeg" -> ".jpg";
			case "image/png" -> ".png";
			case "image/webp" -> ".webp";
			case "image/gif" -> ".gif";
			default -> throw new CustomException(ErrorCode.INVALID_REQUEST);
		};
	}
}
