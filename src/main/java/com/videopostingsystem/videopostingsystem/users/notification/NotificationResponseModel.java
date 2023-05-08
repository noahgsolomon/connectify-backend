package com.videopostingsystem.videopostingsystem.users.notification;

import java.sql.Date;

public record NotificationResponseModel(String sender, String content, NotificationType type, Date date) {
}
