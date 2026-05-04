package com.gochubat.domain.post.service;

import com.gochubat.domain.comment.service.CommentService;
import com.gochubat.domain.like.service.LikeService;
import com.gochubat.domain.post.entity.Post;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PostBoardAssembler {

	private final CommentService commentService;
	private final LikeService likeService;

	public PostBoardAssembler(CommentService commentService, LikeService likeService) {
		this.commentService = commentService;
		this.likeService = likeService;
	}

	public Map<Long, ListCounts> gatherListCounts(List<Post> posts) {
		List<Long> ids = posts.stream().map(Post::getId).toList();
		Map<Long, Long> commentCounts = commentService.countActiveByPostIds(ids);
		Map<Long, Long> likeCounts = likeService.countByPostIds(ids);
		Map<Long, ListCounts> result = new HashMap<>();
		for (Long id : ids) {
			result.put(id, new ListCounts(
					commentCounts.getOrDefault(id, 0L),
					likeCounts.getOrDefault(id, 0L)
			));
		}
		return result;
	}

	public DetailCounts gatherDetailCounts(Long postId, Long viewerId) {
		long commentCount = commentService.countActive(postId);
		long likeCount = likeService.countByPost(postId);
		boolean likedByMe = viewerId != null && likeService.isLikedByMe(postId, viewerId);
		return new DetailCounts(commentCount, likeCount, likedByMe);
	}

	public record ListCounts(long commentCount, long likeCount) {

		public static ListCounts zero() {
			return new ListCounts(0L, 0L);
		}
	}

	public record DetailCounts(long commentCount, long likeCount, boolean likedByMe) {

		public static DetailCounts zero() {
			return new DetailCounts(0L, 0L, false);
		}
	}
}
