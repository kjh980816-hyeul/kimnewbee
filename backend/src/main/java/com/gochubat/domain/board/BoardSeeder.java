package com.gochubat.domain.board;

import com.gochubat.domain.board.entity.Board;
import com.gochubat.domain.board.entity.BoardLayout;
import com.gochubat.domain.board.repository.BoardRepository;
import com.gochubat.domain.user.entity.Tier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class BoardSeeder implements ApplicationRunner {

	private static final List<Seed> SEEDS = List.of(
			new Seed("notice", "공지사항", BoardLayout.LIST, Tier.SEED, Tier.OWNER, 0),
			new Seed("free", "자유게시판", BoardLayout.LIST, Tier.SEED, Tier.PEPPER, 1),
			new Seed("fanart", "팬아트", BoardLayout.GALLERY, Tier.SEED, Tier.PEPPER, 2),
			new Seed("clips", "영상/클립", BoardLayout.VIDEO, Tier.SEED, Tier.PEPPER, 3),
			new Seed("letters", "팬레터", BoardLayout.LETTER, Tier.SEED, Tier.PEPPER, 4),
			new Seed("pets", "반려동물", BoardLayout.GALLERY, Tier.SEED, Tier.PEPPER, 5),
			new Seed("songs", "노래추천", BoardLayout.RANK, Tier.SEED, Tier.PEPPER, 6),
			new Seed("offline", "오프후기", BoardLayout.CARD, Tier.CORN, Tier.CORN, 7)
	);

	private final BoardRepository boardRepository;

	public BoardSeeder(BoardRepository boardRepository) {
		this.boardRepository = boardRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		for (Seed seed : SEEDS) {
			if (!boardRepository.existsBySlug(seed.slug)) {
				boardRepository.save(Board.create(
						seed.slug, seed.name, seed.layout, seed.readTier, seed.writeTier, seed.orderIndex
				));
			}
		}
	}

	private record Seed(String slug, String name, BoardLayout layout, Tier readTier, Tier writeTier, int orderIndex) {
	}
}
