package com.gochubat.domain.song.repository;

import com.gochubat.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

	@Query("select s from Song s join fetch s.submittedBy order by s.createdAt desc, s.id desc")
	List<Song> findAllWithSubmitter();
}
