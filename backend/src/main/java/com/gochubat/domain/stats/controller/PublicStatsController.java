package com.gochubat.domain.stats.controller;

import com.gochubat.domain.post.repository.PostRepository;
import com.gochubat.domain.stats.dto.StatsSummaryResponse;
import com.gochubat.domain.user.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stats")
public class PublicStatsController {

	private final UserRepository userRepository;
	private final PostRepository postRepository;

	public PublicStatsController(UserRepository userRepository, PostRepository postRepository) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
	}

	@GetMapping("/summary")
	public StatsSummaryResponse summary() {
		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
		return new StatsSummaryResponse(
				userRepository.count(),
				postRepository.countByCreatedAtGreaterThanEqual(startOfDay)
		);
	}
}
