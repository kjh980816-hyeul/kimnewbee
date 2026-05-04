package com.gochubat.domain.point;

import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PointService {

	private final UserRepository userRepository;
	private final PointPolicy policy;

	public PointService(UserRepository userRepository, PointPolicy policy) {
		this.userRepository = userRepository;
		this.policy = policy;
	}

	public void award(Long userId, PointReason reason) {
		if (userId == null) {
			return;
		}
		userRepository.findById(userId).ifPresent(user -> {
			user.addPoints(policy.delta(reason));
			Tier next = policy.tierFor(user.getPoints());
			if (user.getTier() != Tier.OWNER) {
				user.promoteTo(next);
			}
		});
	}
}
