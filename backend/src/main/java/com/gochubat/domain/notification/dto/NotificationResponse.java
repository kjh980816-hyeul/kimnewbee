package com.gochubat.domain.notification.dto;

import com.gochubat.domain.notification.entity.Notification;
import com.gochubat.domain.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
		Long id,
		NotificationType type,
		String title,
		String message,
		String link,
		boolean read,
		LocalDateTime createdAt
) {
	public static NotificationResponse from(Notification n) {
		return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getMessage(), n.getLink(), n.isRead(), n.getCreatedAt());
	}
}
