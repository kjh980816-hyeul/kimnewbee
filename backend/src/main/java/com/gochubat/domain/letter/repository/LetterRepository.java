package com.gochubat.domain.letter.repository;

import com.gochubat.domain.letter.entity.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LetterRepository extends JpaRepository<Letter, Long> {

	@Query("select l from Letter l join fetch l.author order by l.createdAt desc, l.id desc")
	List<Letter> findAllWithAuthor();

	@Query("select l from Letter l join fetch l.author where l.id = :id")
	Optional<Letter> findByIdWithAuthor(Long id);
}
