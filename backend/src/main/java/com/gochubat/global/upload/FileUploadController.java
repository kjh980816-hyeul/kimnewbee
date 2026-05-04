package com.gochubat.global.upload;

import com.gochubat.global.security.AuthenticatedController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController extends AuthenticatedController {

	private final UploadService uploadService;

	public FileUploadController(UploadService uploadService) {
		this.uploadService = uploadService;
	}

	@PostMapping("/api/files")
	public ResponseEntity<UploadResponse> upload(
			Authentication authentication,
			@RequestParam("file") MultipartFile file
	) {
		requireUserId(authentication);
		String url = uploadService.store(file);
		return ResponseEntity.status(HttpStatus.CREATED).body(new UploadResponse(url));
	}
}
