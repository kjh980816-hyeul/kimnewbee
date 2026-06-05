package com.gochubat.domain.user.dto;

import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;

import java.time.LocalDateTime;

/**
 * 마이페이지 활동 탭(내 글 / 댓글 단 글 / 좋아요한 글) 공용 목록 아이템.
 * 게시판 타입에 상관없이 동일한 카드로 렌더하기 위한 요약 DTO.
 */
public record MyActivityItemResponse(
		Long id,
		String boardType,
		String title,
		String preview,
		String link,
		String thumbnailUrl,
		long commentCount,
		long likeCount,
		LocalDateTime createdAt
) {

	public static MyActivityItemResponse of(Post post, long commentCount, long likeCount) {
		return new MyActivityItemResponse(
				post.getId(),
				post.getType().name(),
				post.getTitle(),
				post.preview(),
				link(post),
				post.getMediaUrl(),
				commentCount,
				likeCount,
				post.getCreatedAt()
		);
	}

	private static String link(Post post) {
		if (post.getType() == BoardType.CUSTOM) {
			return "/board/" + post.getBoardSlug() + "/post/" + post.getId();
		}
		return "/" + slug(post.getType()) + "/" + post.getId();
	}

	private static String slug(BoardType type) {
		return switch (type) {
			case FREE -> "free";
			case FANART -> "fanart";
			case CLIP -> "clips";
			case PET -> "pets";
			case OFFLINE -> "offline";
			case CUSTOM -> "board";
		};
	}
}
