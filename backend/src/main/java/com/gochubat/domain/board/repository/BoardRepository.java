package com.gochubat.domain.board.repository;

import com.gochubat.domain.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

	List<Board> findAllByOrderByOrderIndexAscIdAsc();

	List<Board> findAllByActiveTrueOrderByOrderIndexAscIdAsc();

	boolean existsBySlug(String slug);
}
