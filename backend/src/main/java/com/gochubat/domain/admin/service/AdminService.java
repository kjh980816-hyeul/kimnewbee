package com.gochubat.domain.admin.service;

import com.gochubat.domain.admin.dto.AdminDashboardResponse;
import com.gochubat.domain.admin.dto.AdminUserResponse;
import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminService {

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final PostLikeRepository postLikeRepository;

	public AdminService(
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

	public AdminDashboardResponse dashboard() {
		return new AdminDashboardResponse(
				userRepository.count(),
				postRepository.count(),
				commentRepository.count(),
				postLikeRepository.count()
		);
	}

	public List<AdminUserResponse> listUsers() {
		return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt", "id")).stream()
				.map(AdminUserResponse::from)
				.toList();
	}

	@Transactional
	public AdminUserResponse changeTier(Long userId, Tier tier) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		user.changeTier(tier);
		return AdminUserResponse.from(user);
	}

	@Transactional
	public AdminUserResponse adjustPoints(Long userId, long delta) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		user.addPoints(delta);
		return AdminUserResponse.from(user);
	}
}
