package com.gochubat.domain.point;

import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PointServiceTest {

	private UserRepository userRepository;
	private PointService pointService;

	@BeforeEach
	void setUp() {
		userRepository = Mockito.mock(UserRepository.class);
		pointService = new PointService(userRepository, new PointPolicy(10, 2, 1, 100, 500));
	}

	@Test
	void award_does_nothing_when_user_id_null() {
		pointService.award(null, PointReason.POST_CREATED);
		Mockito.verifyNoInteractions(userRepository);
	}

	@Test
	void award_does_nothing_when_user_missing() {
		Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());
		pointService.award(99L, PointReason.POST_CREATED);
	}

	@Test
	void award_post_created_adds_10_points_no_tier_change() {
		User user = TestUserFactory.create("nv-x", "x", Tier.SEED);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		pointService.award(1L, PointReason.POST_CREATED);

		assertThat(user.getPoints()).isEqualTo(10L);
		assertThat(user.getTier()).isEqualTo(Tier.SEED);
	}

	@Test
	void award_promotes_seed_to_pepper_at_threshold() {
		User user = TestUserFactory.create("nv-x", "x", Tier.SEED);
		user.addPoints(95L);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		pointService.award(1L, PointReason.POST_CREATED);

		assertThat(user.getPoints()).isEqualTo(105L);
		assertThat(user.getTier()).isEqualTo(Tier.PEPPER);
	}

	@Test
	void award_promotes_pepper_to_corn_at_threshold() {
		User user = TestUserFactory.create("nv-x", "x", Tier.PEPPER);
		user.addPoints(495L);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		pointService.award(1L, PointReason.POST_CREATED);

		assertThat(user.getPoints()).isEqualTo(505L);
		assertThat(user.getTier()).isEqualTo(Tier.CORN);
	}

	@Test
	void award_does_not_demote_or_alter_owner() {
		User user = TestUserFactory.create("nv-o", "o", Tier.OWNER);
		user.addPoints(50L);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		pointService.award(1L, PointReason.POST_CREATED);

		assertThat(user.getPoints()).isEqualTo(60L);
		assertThat(user.getTier()).isEqualTo(Tier.OWNER);
	}

	@Test
	void award_like_revoked_subtracts_but_floor_zero() {
		User user = TestUserFactory.create("nv-x", "x", Tier.SEED);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		pointService.award(1L, PointReason.LIKE_REVOKED);

		assertThat(user.getPoints()).isZero();
	}
}
