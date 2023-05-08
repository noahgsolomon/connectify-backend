package com.videopostingsystem.videopostingsystem.users.notification;

import java.util.Date;

public record NotificationResponseModel(String sender, String content, NotificationType type, Date date) {
}
