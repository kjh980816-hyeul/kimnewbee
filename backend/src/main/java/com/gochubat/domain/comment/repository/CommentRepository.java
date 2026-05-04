package com.gochubat.domain.comment.repository;

import com.gochubat.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	@Query("select c from Comment c join fetch c.author where c.postId = :postId order by c.createdAt asc, c.id asc")
	List<Comment> findByPostIdWithAuthor(@Param("postId") Long postId);

	@Query("select count(c) from Comment c where c.postId = :postId and c.deleted = false")
	long countActiveByPostId(@Param("postId") Long postId);

	@Query("select count(c) from Comment c where c.author.id = :authorId and c.deleted = false")
	long countActiveByAuthorId(@Param("authorId") Long authorId);

	@Query("select c.postId as postId, count(c) as cnt from Comment c "
			+ "where c.postId in :postIds and c.deleted = false group by c.postId")
	List<PostCommentCount> countActiveByPostIds(@Param("postIds") Collection<Long> postIds);

	interface PostCommentCount {
		Long getPostId();

		long getCnt();
	}
}
