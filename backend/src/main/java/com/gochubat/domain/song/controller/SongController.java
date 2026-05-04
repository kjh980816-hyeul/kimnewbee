package com.gochubat.domain.song.controller;

import com.gochubat.domain.song.dto.SongResponse;
import com.gochubat.global.dto.ListResponse;
import com.gochubat.domain.song.dto.SongWriteRequest;
import com.gochubat.domain.song.dto.VoteToggleResponse;
import com.gochubat.domain.song.service.SongService;
import com.gochubat.global.security.AuthenticatedController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
public class SongController extends AuthenticatedController {

	private final SongService songService;

	public SongController(SongService songService) {
		this.songService = songService;
	}

	@GetMapping
	public ListResponse<SongResponse> list(Authentication authentication) {
		return ListResponse.of(songService.list(currentUserIdOrNull(authentication)));
	}

	@PostMapping
	public ResponseEntity<SongResponse> create(
			Authentication authentication,
			@Valid @RequestBody SongWriteRequest request
	) {
		SongResponse response = songService.create(requireUserId(authentication), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{id}/vote")
	public VoteToggleResponse vote(@PathVariable Long id, Authentication authentication) {
		return songService.toggleVote(id, requireUserId(authentication));
	}
}
