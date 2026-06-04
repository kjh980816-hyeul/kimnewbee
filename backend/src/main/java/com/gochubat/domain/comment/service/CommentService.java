package com.gochubat.domain.comment.service;

import com.gochubat.domain.comment.dto.CommentResponse;
import com.gochubat.domain.comment.dto.CommentWriteRequest;
import com.gochubat.domain.comment.entity.Comment;
import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.notification.entity.NotificationType;
import com.gochubat.domain.notification.service.NotificationService;
import com.gochubat.domain.point.PointReason;
import com.gochubat.domain.point.PointService;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
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

	private static final int NOTIFICATION_PREVIEW_LENGTH = 60;

	private final CommentRepository commentRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PointService pointService;
	private final NotificationService notificationService;

	public CommentService(
			CommentRepository commentRepository,
			PostRepository postRepository,
			UserRepository userRepository,
			PointService pointService,
			NotificationService notificationService
	) {
		this.commentRepository = commentRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.pointService = pointService;
		this.notificationService = notificationService;
	}

	public List<CommentResponse> list(Long postId) {
		requirePostExists(postId);
		return commentRepository.findByPostIdWithAuthor(postId).stream()
				.map(CommentResponse::from)
				.toList();
	}

	@Transactional
	public CommentResponse create(Long postId, Long userId, CommentWriteRequest request) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		Comment parent = null;
		if (request.parentId() != null) {
			parent = commentRepository.findById(request.parentId())
					.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
			if (!parent.getPostId().equals(postId)) {
				throw new CustomException(ErrorCode.INVALID_REQUEST);
			}
		}
		User author = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		Comment saved = commentRepository.save(Comment.create(postId, request.parentId(), author, request.content()));
		pointService.award(userId, PointReason.COMMENT_CREATED);
		dispatchCommentNotification(post, author, saved, parent);
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

	private void dispatchCommentNotification(Post post, User commenter, Comment saved, Comment parent) {
		Long postAuthorId = post.getAuthor().getId();
		String link = linkFor(post);
		String snippet = truncate(saved.getContent(), NOTIFICATION_PREVIEW_LENGTH);
		boolean isReply = parent != null;
		Long parentAuthorId = isReply ? parent.getAuthor().getId() : null;
		if (isReply && !parentAuthorId.equals(commenter.getId())) {
			notificationService.notify(
					parentAuthorId,
					NotificationType.REPLY,
					commenter.getNickname() + "님이 대댓글을 남겼어요",
					snippet,
					link
			);
		}
		if (!postAuthorId.equals(commenter.getId()) && (!isReply || !postAuthorId.equals(parentAuthorId))) {
			notificationService.notify(
					postAuthorId,
					NotificationType.COMMENT,
					commenter.getNickname() + "님이 내 글에 댓글을 남겼어요",
					snippet,
					link
			);
		}
	}

	// 알림 클릭 시 이동할 게시글 경로. 커스텀 게시판은 /board/{slug}/post/{id}.
	private String linkFor(Post post) {
		if (post.getType() == BoardType.CUSTOM) {
			return "/board/" + post.getBoardSlug() + "/post/" + post.getId();
		}
		return "/" + boardSlug(post.getType()) + "/" + post.getId();
	}

	private String boardSlug(BoardType type) {
		return switch (type) {
			case FREE -> "free";
			case FANART -> "fanart";
			case CLIP -> "clips";
			case PET -> "pets";
			case OFFLINE -> "offline";
			case CUSTOM -> "board";
		};
	}

	private String truncate(String s, int max) {
		if (s == null) return "";
		if (s.length() <= max) return s;
		return s.substring(0, max) + "...";
	}
}
