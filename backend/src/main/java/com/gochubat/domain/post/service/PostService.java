package com.gochubat.domain.post.service;

import com.gochubat.domain.board.entity.Board;
import com.gochubat.domain.board.repository.BoardRepository;
import com.gochubat.domain.comment.repository.CommentRepository;
import com.gochubat.domain.like.repository.PostLikeRepository;
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

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final PointService pointService;
	private final CommentRepository commentRepository;
	private final PostLikeRepository postLikeRepository;
	private final BoardRepository boardRepository;

	public PostService(
			PostRepository postRepository,
			UserRepository userRepository,
			PointService pointService,
			CommentRepository commentRepository,
			PostLikeRepository postLikeRepository,
			BoardRepository boardRepository
	) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.pointService = pointService;
		this.commentRepository = commentRepository;
		this.postLikeRepository = postLikeRepository;
		this.boardRepository = boardRepository;
	}

	public List<Post> list(BoardType type) {
		return postRepository.findByTypeWithAuthor(type);
	}

	@Transactional
	public Post viewDetail(BoardType type, Long id) {
		Post post = postRepository.findByIdAndTypeWithAuthor(id, type)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		post.incrementViewCount();
		return post;
	}

	// === 관리자가 만든 커스텀 게시판 ===

	public Board requireBoard(String slug) {
		return boardRepository.findBySlug(slug)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
	}

	public List<Post> listCustom(String slug) {
		requireBoard(slug);
		return postRepository.findByBoardSlugWithAuthor(slug);
	}

	@Transactional
	public Post viewDetailCustom(String slug, Long id) {
		Post post = postRepository.findByIdAndBoardSlugWithAuthor(id, slug)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		post.incrementViewCount();
		return post;
	}

	@Transactional
	public Post createCustomPost(String slug, Long userId, String title, String content, String mediaUrl) {
		Board board = requireBoard(slug);
		if (!board.isActive()) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}
		User author = loadAuthor(userId);
		// 등급(Tier) 순서: SEED < PEPPER < CORN < OWNER. 작성 권한 등급 미달이면 차단.
		if (author.getTier().ordinal() < board.getWriteTier().ordinal()) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}
		return save(Post.createCustom(slug, title, content, author, mediaUrl));
	}

	@Transactional
	public Post save(Post post) {
		Post saved = postRepository.save(post);
		pointService.award(saved.getAuthor().getId(), PointReason.POST_CREATED);
		return saved;
	}

	public User loadAuthor(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
	}

	// 작성자 본인이거나 관리자(OWNER)면 게시판 종류 무관하게 삭제. 연관 댓글/좋아요도 함께 정리.
	@Transactional
	public void deleteByRequester(Long postId, Long requesterId, boolean isOwner) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		if (!isOwner && !post.getAuthor().getId().equals(requesterId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}
		commentRepository.deleteByPostId(postId);
		postLikeRepository.deleteByPostId(postId);
		postRepository.deleteById(postId);
	}
}
