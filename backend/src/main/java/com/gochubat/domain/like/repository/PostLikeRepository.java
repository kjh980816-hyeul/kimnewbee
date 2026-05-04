package com.gochubat.domain.like.repository;

import com.gochubat.domain.like.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

	long countByPostId(Long postId);

	long countByUserId(Long userId);

	@Query("select pl.postId as postId, count(pl) as cnt from PostLike pl "
			+ "where pl.postId in :postIds group by pl.postId")
	List<PostLikeCount> countByPostIds(@Param("postIds") Collection<Long> postIds);

	interface PostLikeCount {
		Long getPostId();

		long getCnt();
	}
}
