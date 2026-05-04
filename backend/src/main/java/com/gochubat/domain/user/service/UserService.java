package com.gochubat.domain.user.service;

import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final PostLikeRepository postLikeRepository;

	public UserService(
			UserRepository userRepository,
			PostRepository postRepository,
			CommentRepository commentRepository,
			PostLikeRepository postLikeRepository
	) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.postLikeRepository = postLikeRepository;
	}

	public CurrentUserResponse getCurrentUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		return CurrentUserResponse.from(user);
	}

	public UserStatsResponse getStats(Long userId) {
		userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		return new UserStatsResponse(
				postRepository.countByAuthorId(userId),
				commentRepository.countActiveByAuthorId(userId),
				postLikeRepository.countByUserId(userId),
				0L
		);
	}
}
