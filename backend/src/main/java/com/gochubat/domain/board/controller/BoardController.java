package com.gochubat.domain.board.controller;

import com.gochubat.domain.board.dto.BoardCreateRequest;
import com.gochubat.domain.board.dto.BoardReorderRequest;
import com.gochubat.domain.board.dto.BoardResponse;
import com.gochubat.domain.board.dto.BoardUpdateRequest;
import com.gochubat.domain.board.service.BoardService;
import com.gochubat.global.dto.ListResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoardController {

	private final BoardService boardService;

	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}

	@GetMapping("/api/boards")
	public ListResponse<BoardResponse> publicList() {
		return ListResponse.of(boardService.listActive());
	}

	@GetMapping("/api/admin/boards")
	@PreAuthorize("hasRole('OWNER')")
	public ListResponse<BoardResponse> adminList() {
		return ListResponse.of(boardService.listAll());
	}

	@PostMapping("/api/admin/boards")
	@PreAuthorize("hasRole('OWNER')")
	public ResponseEntity<BoardResponse> create(@Valid @RequestBody BoardCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(boardService.create(request));
	}

	@PatchMapping("/api/admin/boards/{id}")
	@PreAuthorize("hasRole('OWNER')")
	public BoardResponse update(@PathVariable Long id, @Valid @RequestBody BoardUpdateRequest request) {
		return boardService.update(id, request);
	}

	@DeleteMapping("/api/admin/boards/{id}")
	@PreAuthorize("hasRole('OWNER')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		boardService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/api/admin/boards/order")
	@PreAuthorize("hasRole('OWNER')")
	public ListResponse<BoardResponse> reorder(@Valid @RequestBody BoardReorderRequest request) {
		return ListResponse.of(boardService.reorder(request.orderedIds()));
	}
}
