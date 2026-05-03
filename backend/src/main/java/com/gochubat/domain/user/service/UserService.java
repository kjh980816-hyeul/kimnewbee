package com.gochubat.domain.user.service;

import com.gochubat.domain.user.dto.CurrentUserResponse;
import com.gochubat.domain.user.dto.UserStatsResponse;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public CurrentUserResponse getCurrentUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		return CurrentUserResponse.from(user);
	}

	public UserStatsResponse getStats(Long userId) {
		userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
		return UserStatsResponse.zero();
	}
}
