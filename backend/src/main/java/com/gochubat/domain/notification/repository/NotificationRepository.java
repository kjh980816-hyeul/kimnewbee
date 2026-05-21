package com.gochubat.domain.notification.repository;

import com.gochubat.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	@Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId ORDER BY n.createdAt DESC, n.id DESC")
	List<Notification> findRecentByRecipient(@Param("recipientId") Long recipientId, Pageable pageable);

	long countByRecipientIdAndReadFalse(Long recipientId);

	@Modifying
	@Query("UPDATE Notification n SET n.read = true WHERE n.recipientId = :recipientId AND n.read = false")
	int markAllReadByRecipient(@Param("recipientId") Long recipientId);
}
