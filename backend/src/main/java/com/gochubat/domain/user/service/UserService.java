package com.gochubat.domain.user.service;

import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.like.repository.PostLikeRepository;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.MyActivityItemResponse;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

	private static final Duration NICKNAME_CHANGE_INTERVAL = Duration.ofDays(30);

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

	@Transactional
	public CurrentUserResponse updateProfileImage(Long userId, String profileImage) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		user.changeProfileImage(profileImage, LocalDateTime.now());
		return CurrentUserResponse.from(user);
	}

	@Transactional
	public CurrentUserResponse updateNickname(Long userId, String rawNickname) {
		String nickname = rawNickname == null ? "" : rawNickname.trim();
		if (nickname.isEmpty()) {
			throw new CustomException(ErrorCode.INVALID_REQUEST);
		}
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		if (nickname.equals(user.getNickname())) {
			return CurrentUserResponse.from(user);
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime last = user.getNicknameChangedAt();
		if (last != null && Duration.between(last, now).compareTo(NICKNAME_CHANGE_INTERVAL) < 0) {
			throw new CustomException(ErrorCode.NICKNAME_TOO_SOON);
		}
		if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
			throw new CustomException(ErrorCode.NICKNAME_DUPLICATED);
		}
		user.changeNickname(nickname, now);
		return CurrentUserResponse.from(user);
	}

	public UserStatsResponse getStats(Long userId) {
		requireUser(userId);
		return new UserStatsResponse(
				postRepository.countByAuthorId(userId),
				commentRepository.countActiveByAuthorId(userId),
				postLikeRepository.countByUserId(userId),
				0L
		);
	}

	public List<MyActivityItemResponse> getMyPosts(Long userId) {
		requireUser(userId);
		return toItems(postRepository.findByAuthorIdWithAuthor(userId));
	}

	public List<MyActivityItemResponse> getCommentedPosts(Long userId) {
		requireUser(userId);
		return toItemsInOrder(commentRepository.findDistinctPostIdsByAuthorIdOrderByLatest(userId));
	}

	public List<MyActivityItemResponse> getLikedPosts(Long userId) {
		requireUser(userId);
		return toItemsInOrder(postLikeRepository.findPostIdsByUserId(userId));
	}

	private User requireUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
	}

	// postId 순서를 보존하며 게시글을 로드(삭제돼 사라진 글은 건너뜀).
	private List<MyActivityItemResponse> toItemsInOrder(List<Long> orderedPostIds) {
		if (orderedPostIds.isEmpty()) {
			return List.of();
		}
		Map<Long, Post> byId = postRepository.findByIdInWithAuthor(orderedPostIds).stream()
				.collect(Collectors.toMap(Post::getId, Function.identity()));
		List<Post> ordered = orderedPostIds.stream()
				.map(byId::get)
				.filter(Objects::nonNull)
				.toList();
		return toItems(ordered);
	}

	private List<MyActivityItemResponse> toItems(List<Post> posts) {
		if (posts.isEmpty()) {
			return List.of();
		}
		List<Long> ids = posts.stream().map(Post::getId).toList();
		Map<Long, Long> commentCounts = commentRepository.countActiveByPostIds(ids).stream()
				.collect(Collectors.toMap(
						CommentRepository.PostCommentCount::getPostId,
						CommentRepository.PostCommentCount::getCnt));
		Map<Long, Long> likeCounts = postLikeRepository.countByPostIds(ids).stream()
				.collect(Collectors.toMap(
						PostLikeRepository.PostLikeCount::getPostId,
						PostLikeRepository.PostLikeCount::getCnt));
		return posts.stream()
				.map(post -> MyActivityItemResponse.of(
						post,
						commentCounts.getOrDefault(post.getId(), 0L),
						likeCounts.getOrDefault(post.getId(), 0L)))
				.toList();
	}
}
