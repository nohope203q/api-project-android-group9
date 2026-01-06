package com.api.group9.service;

import com.api.group9.dto.Response.NotificationResponse;
import com.api.group9.model.Notification;
import com.api.group9.model.User;
import com.api.group9.enums.NotificationType;
import com.api.group9.repository.NotificationRepository;
import com.api.group9.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;

    public void sendNotification(User sender, User recipient, NotificationType type, Long relatedId) {
        if (sender.getId().equals(recipient.getId())) return;

        Notification noti = new Notification();
        noti.setSender(sender);
        noti.setRecipient(recipient);
        noti.setType(type);
        noti.setRelatedId(relatedId);

        // Tạo nội dung tin nhắn tùy loại
        switch (type) {
            case LIKE_POST:
                noti.setMessage("đã thích bài viết của bạn.");
                break;
            case COMMENT_POST:
                noti.setMessage("đã bình luận về bài viết của bạn.");
                break;
            case FRIEND_REQUEST:
                noti.setMessage("đã gửi lời mời kết bạn.");
                break;
             case ACCEPT_FRIEND:
                noti.setMessage("đã chấp nhận lời mời kết bạn.");
                break;
        }

        notificationRepository.save(noti);
    }

    // 2. Lấy danh sách thông báo của User đang đăng nhập
    public List<NotificationResponse> getMyNotifications() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsernameOrEmail(currentUsername, currentUsername).orElseThrow();

        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId());
        return list.stream().map(NotificationResponse::new).collect(Collectors.toList());
    }
    
    // 3. Đánh dấu đã đọc (Khi user bấm vào thông báo)
    public void markAsRead(Long notificationId) {
        Notification noti = notificationRepository.findById(notificationId).orElse(null);
        if (noti != null) {
            noti.setRead(true);
            notificationRepository.save(noti);
        }
    }
}