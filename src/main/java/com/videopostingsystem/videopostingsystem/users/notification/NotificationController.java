package com.videopostingsystem.videopostingsystem.users.notification;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(HttpSession session){
        return notificationService.getNotifications(session);
    }

    @DeleteMapping("/delete-notifications")
    public ResponseEntity<?> removeAllNotifications(HttpSession session){
        return notificationService.removeAllNotifications(session);
    }
}
