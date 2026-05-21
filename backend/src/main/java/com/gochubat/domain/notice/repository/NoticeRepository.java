package com.gochubat.domain.notice.repository;

import com.gochubat.domain.notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

	@Query("select n from Notice n join fetch n.author order by n.createdAt desc, n.id desc")
	List<Notice> findAllWithAuthor();

	@Query("select n from Notice n join fetch n.author where n.id = :id")
	Optional<Notice> findByIdWithAuthor(Long id);

	@Query("select n from Notice n join fetch n.author " +
			"where lower(n.title) like lower(concat('%', :q, '%')) or n.content like concat('%', :q, '%') " +
			"order by n.createdAt desc, n.id desc")
	List<Notice> search(@Param("q") String q, Pageable pageable);
}
