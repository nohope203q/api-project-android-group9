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
        if (sender.getId().equals(recipient.getId())) return; // Không tự thông báo cho chính mình

        Notification noti = new Notification();
        noti.setSender(sender);
        noti.setRecipient(recipient);
        noti.setType(type);
        noti.setRelatedId(relatedId);

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
                
            case FRIEND_ACCEPT: 
                noti.setMessage("đã chấp nhận lời mời kết bạn.");
                break;
                
            case COMMENT_REPLY:
                noti.setMessage("đã trả lời bình luận của bạn.");
                break;
                
            default:
                noti.setMessage("đã có tương tác mới.");
                break;
        }

        notificationRepository.save(noti);
    }

    // 2. Lấy danh sách thông báo
    public List<NotificationResponse> getMyNotifications() {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        // Lưu ý: Đảm bảo Repo có hàm findByEmail nhé
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId());
        
        // Đoạn này nó sẽ gọi cái Constructor mầy vừa sửa ở bước trước
        // -> senderId sẽ được map vào DTO -> Gửi về Android
        return list.stream().map(NotificationResponse::new).collect(Collectors.toList());
    }
    
    // 3. Đánh dấu đã đọc
    public void markAsRead(Long notificationId) {
        // Dùng ifPresent cho gọn và an toàn
        notificationRepository.findById(notificationId).ifPresent(noti -> {
            noti.setRead(true);
            notificationRepository.save(noti);
        });
    }
}