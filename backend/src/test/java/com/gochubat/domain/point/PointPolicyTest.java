package com.gochubat.domain.point;

import com.gochubat.domain.user.entity.Tier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PointPolicyTest {

	private PointPolicy policy;

	@BeforeEach
	void setUp() {
		policy = new PointPolicy(10, 2, 1, 100, 500);
	}

	@Test
	void delta_returns_configured_values_per_reason() {
		assertThat(policy.delta(PointReason.POST_CREATED)).isEqualTo(10L);
		assertThat(policy.delta(PointReason.COMMENT_CREATED)).isEqualTo(2L);
		assertThat(policy.delta(PointReason.LIKE_RECEIVED)).isEqualTo(1L);
		assertThat(policy.delta(PointReason.LIKE_REVOKED)).isEqualTo(-1L);
	}

	@Test
	void tier_for_returns_seed_below_pepper_threshold() {
		assertThat(policy.tierFor(0L)).isEqualTo(Tier.SEED);
		assertThat(policy.tierFor(99L)).isEqualTo(Tier.SEED);
	}

	@Test
	void tier_for_returns_pepper_at_threshold() {
		assertThat(policy.tierFor(100L)).isEqualTo(Tier.PEPPER);
		assertThat(policy.tierFor(499L)).isEqualTo(Tier.PEPPER);
	}

	@Test
	void tier_for_returns_corn_at_threshold() {
		assertThat(policy.tierFor(500L)).isEqualTo(Tier.CORN);
		assertThat(policy.tierFor(99999L)).isEqualTo(Tier.CORN);
	}
}
