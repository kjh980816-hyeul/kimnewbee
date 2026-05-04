package com.gochubat.domain.like.service;

import com.gochubat.domain.like.dto.LikeToggleResponse;
import com.gochubat.domain.like.entity.PostLike;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LikeService {

	private final PostLikeRepository postLikeRepository;
	private final PostRepository postRepository;

	public LikeService(PostLikeRepository postLikeRepository, PostRepository postRepository) {
		this.postLikeRepository = postLikeRepository;
		this.postRepository = postRepository;
	}

	@Transactional
	public LikeToggleResponse toggle(BoardType type, Long postId, Long userId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		if (post.getType() != type) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}
		Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
		boolean liked;
		if (existing.isPresent()) {
			postLikeRepository.delete(existing.get());
			liked = false;
		} else {
			postLikeRepository.save(PostLike.create(postId, userId));
			liked = true;
		}
		return new LikeToggleResponse(liked, postLikeRepository.countByPostId(postId));
	}

	public long countByPost(Long postId) {
		return postLikeRepository.countByPostId(postId);
	}

	public Map<Long, Long> countByPostIds(Collection<Long> postIds) {
		if (postIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, Long> result = new HashMap<>();
		for (PostLikeRepository.PostLikeCount row : postLikeRepository.countByPostIds(postIds)) {
			result.put(row.getPostId(), row.getCnt());
		}
		return result;
	}

	public boolean isLikedByMe(Long postId, Long userId) {
		return postLikeRepository.findByPostIdAndUserId(postId, userId).isPresent();
	}
}
