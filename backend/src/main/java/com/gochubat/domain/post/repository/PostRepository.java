package com.gochubat.domain.post.repository;

import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query("select p from Post p join fetch p.author where p.type = :type order by p.createdAt desc, p.id desc")
	List<Post> findByTypeWithAuthor(@Param("type") BoardType type);

	@Query("select p from Post p join fetch p.author where p.id = :id and p.type = :type")
	Optional<Post> findByIdAndTypeWithAuthor(@Param("id") Long id, @Param("type") BoardType type);

	long countByAuthorId(Long authorId);

	@Query("select p from Post p join fetch p.author where p.type in :types " +
			"and (lower(p.title) like lower(concat('%', :q, '%')) or p.content like concat('%', :q, '%')) " +
			"order by p.createdAt desc, p.id desc")
	List<Post> search(@Param("q") String q, @Param("types") Collection<BoardType> types, Pageable pageable);
}
