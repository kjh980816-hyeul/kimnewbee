package com.gochubat.domain.song.service;

import com.gochubat.domain.song.dto.SongResponse;
import com.gochubat.domain.song.dto.SongWriteRequest;
import com.gochubat.domain.song.dto.VoteToggleResponse;
import com.gochubat.domain.song.entity.Song;
import com.gochubat.domain.song.entity.SongVote;
import com.gochubat.domain.song.repository.SongRepository;
import com.gochubat.domain.song.repository.SongVoteRepository;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class SongService {

	private final SongRepository songRepository;
	private final SongVoteRepository songVoteRepository;
	private final UserRepository userRepository;

	public SongService(
			SongRepository songRepository,
			SongVoteRepository songVoteRepository,
			UserRepository userRepository
	) {
		this.songRepository = songRepository;
		this.songVoteRepository = songVoteRepository;
		this.userRepository = userRepository;
	}

	public List<SongResponse> list(Long viewerId) {
		List<Song> songs = songRepository.findAllWithSubmitter();
		List<Long> songIds = songs.stream().map(Song::getId).toList();
		Map<Long, Long> voteCounts = countMap(songIds);
		Set<Long> votedByMe = viewerId == null
				? Set.of()
				: new HashSet<>(songVoteRepository.findVotedSongIds(songIds, viewerId));
		return songs.stream()
				.map(s -> SongResponse.from(
						s,
						voteCounts.getOrDefault(s.getId(), 0L),
						votedByMe.contains(s.getId())
				))
				.toList();
	}

	@Transactional
	public SongResponse create(Long userId, SongWriteRequest request) {
		User submitter = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		Song saved = songRepository.save(
				Song.create(request.title(), request.artist(), request.link(), submitter)
		);
		return SongResponse.from(saved, 0L, false);
	}

	@Transactional
	public VoteToggleResponse toggleVote(Long songId, Long userId) {
		if (!songRepository.existsById(songId)) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}
		Optional<SongVote> existing = songVoteRepository.findBySongIdAndUserId(songId, userId);
		boolean voted;
		if (existing.isPresent()) {
			songVoteRepository.delete(existing.get());
			voted = false;
		} else {
			songVoteRepository.save(SongVote.create(songId, userId));
			voted = true;
		}
		return new VoteToggleResponse(voted, songVoteRepository.countBySongId(songId));
	}

	private Map<Long, Long> countMap(List<Long> songIds) {
		if (songIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, Long> map = new HashMap<>();
		for (SongVoteRepository.SongVoteCount row : songVoteRepository.countBySongIds(songIds)) {
			map.put(row.getSongId(), row.getCnt());
		}
		return map;
	}
}
