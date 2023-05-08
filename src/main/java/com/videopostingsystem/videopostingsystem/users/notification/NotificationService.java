package com.videopostingsystem.videopostingsystem.users.notification;

import com.google.gson.Gson;
import com.videopostingsystem.videopostingsystem.users.UserRepository;
import com.videopostingsystem.videopostingsystem.users.Users;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ResponseEntity<?> getNotifications(HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (session.getAttribute("loggedInUser") == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        Users user = userRepository.findById(loggedInUser).get();
        List<Notification> notifications = notificationRepository.findAllByUsers(user);
        List<NotificationResponseModel> notificationResponseModel = new ArrayList<>();
        for (Notification notification : notifications){
            notificationResponseModel.add(new NotificationResponseModel(notification.getSender().getUsername(), notification.getContent(), notification.getType(), notification.getTime()));
        }
        Gson gson = new Gson();
        return ResponseEntity.ok(gson.toJson(notificationResponseModel));
    }

    public ResponseEntity<?> removeAllNotifications(HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (session.getAttribute("loggedInUser") == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        Users user = userRepository.findById(loggedInUser).get();
        List<Notification> userNotifications = notificationRepository.findAllByUsers(user);
        for (Notification notification: userNotifications){
            notificationRepository.deleteById(notification.getId());
        }
        return ResponseEntity.ok("Removed all notifications");
    }

    public void setNotification(Users notificationSender, Users notificationReceiver, NotificationType type, Long objectId){
        String content = "";
        if (type == NotificationType.COMMENT){
            content =  "commented on your post.";
        } else if (type == NotificationType.FOLLOW) {
            content = "started following you.";
        } else if (type == NotificationType.LIKE) {
            content = "liked your post.";
        } else if (type == NotificationType.TAG) {
            content = "mentioned you in a comment.";
        } else if (type == NotificationType.MESSAGE) {
            content = "sent you a message.";
        }
        Notification newNotification = new Notification(notificationReceiver, notificationSender, type, content, objectId);
        notificationRepository.save(newNotification);
    }

    public void removeNotification(Users notificationSender, Users notificationReceiver, NotificationType type, Long objectId){
            List<Notification> notifications = notificationRepository.findByTypeAndRelatedObjectIdAndSenderAndUsers(type, objectId, notificationSender, notificationReceiver);
            for (Notification notification : notifications){
                notificationRepository.deleteById(notification.getId());
            }
    }
}
