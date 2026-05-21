package com.gochubat.domain.notification.controller;

import com.gochubat.domain.notification.dto.NotificationResponse;
import com.gochubat.domain.notification.dto.UnreadCountResponse;
import com.gochubat.domain.notification.service.NotificationService;
import com.gochubat.global.dto.ListResponse;
import com.gochubat.global.security.AuthenticatedController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/notifications")
@RequiredArgsConstructor
public class NotificationController extends AuthenticatedController {

	private final NotificationService notificationService;

	@GetMapping
	public ListResponse<NotificationResponse> list(Authentication auth) {
		return notificationService.listRecent(requireUserId(auth));
	}

	@GetMapping("/unread-count")
	public UnreadCountResponse unreadCount(Authentication auth) {
		return notificationService.unreadCount(requireUserId(auth));
	}

	@PostMapping("/{id}/read")
	public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication auth) {
		notificationService.markRead(requireUserId(auth), id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/read-all")
	public ResponseEntity<Void> markAllRead(Authentication auth) {
		notificationService.markAllRead(requireUserId(auth));
		return ResponseEntity.noContent().build();
	}
}
