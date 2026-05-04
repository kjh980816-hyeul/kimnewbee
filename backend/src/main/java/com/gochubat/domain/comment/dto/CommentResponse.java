package com.gochubat.domain.comment.dto;

import com.gochubat.domain.comment.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
		Long id,
		Long postId,
		Long parentId,
		String author,
		String content,
		LocalDateTime createdAt,
		long likeCount,
		boolean deleted
) {

	private static final String DELETED_PLACEHOLDER = "삭제된 댓글이에요";

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getPostId(),
				comment.getParentId(),
				comment.getAuthor().getNickname(),
				comment.isDeleted() ? DELETED_PLACEHOLDER : comment.getContent(),
				comment.getCreatedAt(),
				0L,
				comment.isDeleted()
		);
	}
}
