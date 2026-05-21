package com.gochubat.domain.notification.service;

import com.gochubat.domain.notification.dto.NotificationResponse;
import com.gochubat.domain.notification.dto.UnreadCountResponse;
import com.gochubat.domain.notification.entity.Notification;
import com.gochubat.domain.notification.entity.NotificationType;
import com.gochubat.domain.notification.repository.NotificationRepository;
import com.gochubat.global.dto.ListResponse;
import com.gochubat.global.exception.CustomException;
import com.gochubat.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private static final int DEFAULT_PAGE_SIZE = 30;

	private final NotificationRepository notificationRepository;

	@Transactional
	public Notification notify(Long recipientId, NotificationType type, String title, String message, String link) {
		if (recipientId == null) {
			return null;
		}
		return notificationRepository.save(Notification.create(recipientId, type, title, message, link));
	}

	@Transactional(readOnly = true)
	public ListResponse<NotificationResponse> listRecent(Long recipientId) {
		List<Notification> rows = notificationRepository.findRecentByRecipient(recipientId, PageRequest.of(0, DEFAULT_PAGE_SIZE));
		List<NotificationResponse> data = rows.stream().map(NotificationResponse::from).toList();
		return new ListResponse<>(data, data.size());
	}

	@Transactional(readOnly = true)
	public UnreadCountResponse unreadCount(Long recipientId) {
		return new UnreadCountResponse(notificationRepository.countByRecipientIdAndReadFalse(recipientId));
	}

	@Transactional
	public void markRead(Long recipientId, Long notificationId) {
		Notification n = notificationRepository.findById(notificationId)
				.orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
		if (!n.isOwnedBy(recipientId)) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}
		if (!n.isRead()) {
			n.markRead();
		}
	}

	@Transactional
	public int markAllRead(Long recipientId) {
		return notificationRepository.markAllReadByRecipient(recipientId);
	}
}
