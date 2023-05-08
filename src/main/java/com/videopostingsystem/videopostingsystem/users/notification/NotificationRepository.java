package com.videopostingsystem.videopostingsystem.users.notification;

import com.videopostingsystem.videopostingsystem.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUsers(Users users);
    List<Notification> findByTypeAndRelatedObjectIdAndSenderAndUsers(NotificationType type, Long relatedObjectId, Users sender, Users users);
}
