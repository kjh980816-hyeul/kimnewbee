package com.gochubat.domain.point;

import com.gochubat.domain.user.entity.Tier;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gochubat.point")
public record PointPolicy(
		long postCreated,
		long commentCreated,
		long likeReceived,
		long pepperThreshold,
		long cornThreshold
) {

	public long delta(PointReason reason) {
		return switch (reason) {
			case POST_CREATED -> postCreated;
			case COMMENT_CREATED -> commentCreated;
			case LIKE_RECEIVED -> likeReceived;
			case LIKE_REVOKED -> -likeReceived;
		};
	}

	public Tier tierFor(long points) {
		if (points >= cornThreshold) {
			return Tier.CORN;
		}
		if (points >= pepperThreshold) {
			return Tier.PEPPER;
		}
		return Tier.SEED;
	}
}
