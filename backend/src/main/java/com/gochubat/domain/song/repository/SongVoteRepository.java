package com.gochubat.domain.song.repository;

import com.gochubat.domain.song.entity.SongVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SongVoteRepository extends JpaRepository<SongVote, Long> {

	Optional<SongVote> findBySongIdAndUserId(Long songId, Long userId);

	long countBySongId(Long songId);

	@Query("select sv.songId as songId, count(sv) as cnt from SongVote sv "
			+ "where sv.songId in :songIds group by sv.songId")
	List<SongVoteCount> countBySongIds(@Param("songIds") Collection<Long> songIds);

	@Query("select sv.songId from SongVote sv "
			+ "where sv.songId in :songIds and sv.userId = :userId")
	List<Long> findVotedSongIds(@Param("songIds") Collection<Long> songIds, @Param("userId") Long userId);

	interface SongVoteCount {
		Long getSongId();

		long getCnt();
	}
}
