package com.gochubat.domain.board.service;

import com.gochubat.domain.board.dto.BoardCreateRequest;
import com.gochubat.domain.board.dto.BoardResponse;
import com.gochubat.domain.board.dto.BoardUpdateRequest;
import com.gochubat.domain.board.entity.Board;
import com.gochubat.domain.board.repository.BoardRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BoardService {

	private final BoardRepository boardRepository;

	public BoardService(BoardRepository boardRepository) {
		this.boardRepository = boardRepository;
	}

	public List<BoardResponse> listAll() {
		return boardRepository.findAllByOrderByOrderIndexAscIdAsc().stream()
				.map(BoardResponse::from)
				.toList();
	}

	public List<BoardResponse> listActive() {
		return boardRepository.findAllByActiveTrueOrderByOrderIndexAscIdAsc().stream()
				.map(BoardResponse::from)
				.toList();
	}

	@Transactional
	public BoardResponse create(BoardCreateRequest request) {
		if (boardRepository.existsBySlug(request.slug())) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
		int nextOrder = boardRepository.findAllByOrderByOrderIndexAscIdAsc().size();
		Board saved = boardRepository.save(Board.create(
				request.slug(),
				request.name(),
				request.layout(),
				request.readTier(),
				request.writeTier(),
				nextOrder
		));
		return BoardResponse.from(saved);
	}

	@Transactional
	public BoardResponse update(Long id, BoardUpdateRequest request) {
		Board board = boardRepository.findById(id)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		board.rename(request.name());
		board.changeLayout(request.layout());
		board.changeTiers(request.readTier(), request.writeTier());
		board.setActive(Boolean.TRUE.equals(request.active()));
		return BoardResponse.from(board);
	}

	@Transactional
	public void delete(Long id) {
		if (!boardRepository.existsById(id)) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}
		boardRepository.deleteById(id);
	}

	@Transactional
	public List<BoardResponse> reorder(List<Long> orderedIds) {
		List<Board> existing = boardRepository.findAllByOrderByOrderIndexAscIdAsc();
		if (existing.size() != orderedIds.size()) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
		Map<Long, Board> byId = new HashMap<>();
		existing.forEach(b -> byId.put(b.getId(), b));

		for (int i = 0; i < orderedIds.size(); i++) {
			Board board = byId.get(orderedIds.get(i));
			if (board == null) {
				throw new CustomException(ErrorCode.INVALID_REQUEST);
			}
			board.changeOrder(i);
		}
		return listAll();
	}
}
