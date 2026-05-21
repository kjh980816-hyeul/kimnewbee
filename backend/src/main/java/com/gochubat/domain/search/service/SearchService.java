package com.gochubat.domain.search.service;

import com.gochubat.domain.notice.entity.Notice;
import com.gochubat.domain.notice.repository.NoticeRepository;
import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.search.dto.SearchHit;
import com.gochubat.domain.search.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

	private static final int PER_TYPE_LIMIT = 20;
	private static final int SNIPPET_LENGTH = 80;
	private static final Map<BoardType, String> BOARD_SLUG = Map.of(
			BoardType.FREE, "free",
			BoardType.FANART, "fanart",
			BoardType.CLIP, "clips",
			BoardType.PET, "pets",
			BoardType.OFFLINE, "offline"
	);
	private static final Map<BoardType, String> BOARD_LABEL = Map.of(
			BoardType.FREE, "자유게시판",
			BoardType.FANART, "팬아트",
			BoardType.CLIP, "클립",
			BoardType.PET, "반려동물",
			BoardType.OFFLINE, "오프 후기"
	);

	private final NoticeRepository noticeRepository;
	private final PostRepository postRepository;

	@Transactional(readOnly = true)
	public SearchResponse search(String rawQuery, String typeFilter) {
		String q = rawQuery == null ? "" : rawQuery.trim();
		if (q.isEmpty()) {
			return new SearchResponse(q, 0, List.of());
		}

		List<SearchHit> hits = new ArrayList<>();
		boolean includeNotice = typeFilter == null || typeFilter.isBlank() || typeFilter.equals("notice");
		boolean includePost = typeFilter == null || typeFilter.isBlank() || typeFilter.equals("post");

		if (includeNotice) {
			for (Notice n : noticeRepository.search(q, PageRequest.of(0, PER_TYPE_LIMIT))) {
				hits.add(new SearchHit(
						"notice",
						n.getId(),
						n.getTitle(),
						snippet(n.getContent(), q),
						"notices",
						"공지사항",
						n.getAuthor().getNickname(),
						n.getCreatedAt()
				));
			}
		}

		if (includePost) {
			Collection<BoardType> types = Set.of(BoardType.FREE, BoardType.FANART, BoardType.CLIP, BoardType.PET, BoardType.OFFLINE);
			for (Post p : postRepository.search(q, types, PageRequest.of(0, PER_TYPE_LIMIT))) {
				hits.add(new SearchHit(
						"post",
						p.getId(),
						p.getTitle(),
						snippet(p.getContent(), q),
						BOARD_SLUG.get(p.getType()),
						BOARD_LABEL.get(p.getType()),
						p.getAuthor().getNickname(),
						p.getCreatedAt()
				));
			}
		}

		hits.sort(Comparator.comparing(SearchHit::createdAt).reversed());
		return new SearchResponse(q, hits.size(), hits);
	}

	private String snippet(String content, String q) {
		if (content == null) return "";
		String lower = content.toLowerCase();
		int idx = lower.indexOf(q.toLowerCase());
		if (idx < 0) {
			return content.length() <= SNIPPET_LENGTH ? content : content.substring(0, SNIPPET_LENGTH) + "...";
		}
		int start = Math.max(0, idx - 20);
		int end = Math.min(content.length(), idx + SNIPPET_LENGTH);
		String s = content.substring(start, end);
		if (start > 0) s = "..." + s;
		if (end < content.length()) s = s + "...";
		return s;
	}
}
