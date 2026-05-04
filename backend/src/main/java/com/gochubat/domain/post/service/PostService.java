package com.gochubat.domain.post.service;

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

	public PostService(PostRepository postRepository, UserRepository userRepository, PointService pointService) {
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.pointService = pointService;
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
}
