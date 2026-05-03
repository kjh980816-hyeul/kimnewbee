package com.gochubat.support;

import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;

import java.lang.reflect.Field;

public final class TestUserFactory {

	private TestUserFactory() {
	}

	public static User create(String naverId, String nickname, Tier tier) {
		User user = User.createFromNaver(naverId, nickname, null);
		setField(user, "tier", tier);
		return user;
	}

	public static void setId(User user, long id) {
		setField(user, "id", id);
	}

	private static void setField(Object target, String name, Object value) {
		try {
			Field field = User.class.getDeclaredField(name);
			field.setAccessible(true);
			field.set(target, value);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
