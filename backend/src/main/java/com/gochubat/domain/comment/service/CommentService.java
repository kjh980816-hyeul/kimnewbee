package com.gochubat.domain.comment.service;

import com.gochubat.domain.comment.dto.CommentResponse;
import com.gochubat.domain.comment.dto.CommentWriteRequest;
import com.gochubat.domain.comment.entity.Comment;
import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CommentService {

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	public CommentService(
			CommentRepository commentRepository,
			PostRepository postRepository,
			UserRepository userRepository
	) {
		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
	}

	public List<CommentResponse> list(Long postId) {
		requirePostExists(postId);
		return commentRepository.findByPostIdWithAuthor(postId).stream()
				.map(CommentResponse::from)
				.toList();
	}

	@Transactional
	public CommentResponse create(Long postId, Long userId, CommentWriteRequest request) {
		requirePostExists(postId);
		if (request.parentId() != null) {
			Comment parent = commentRepository.findById(request.parentId())
					.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
			if (!parent.getPostId().equals(postId)) {
				throw new CustomException(ErrorCode.INVALID_REQUEST);
			}
		}
		User author = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		Comment saved = commentRepository.save(Comment.create(postId, request.parentId(), author, request.content()));
		return CommentResponse.from(saved);
	}

	@Transactional
	public void softDelete(Long commentId, Long userId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		if (!comment.isOwnedBy(userId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}
		comment.softDelete();
	}

	public long countActive(Long postId) {
		return commentRepository.countActiveByPostId(postId);
	}

	public Map<Long, Long> countActiveByPostIds(Collection<Long> postIds) {
		if (postIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, Long> result = new HashMap<>();
		for (CommentRepository.PostCommentCount row : commentRepository.countActiveByPostIds(postIds)) {
			result.put(row.getPostId(), row.getCnt());
		}
		return result;
	}

	private void requirePostExists(Long postId) {
		if (!postRepository.existsById(postId)) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}
	}
}
