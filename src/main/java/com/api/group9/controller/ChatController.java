package com.api.group9.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.api.group9.model.Message;
import com.api.group9.repository.MessageRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @MessageMapping("/chat")
    public void processMessage(@Payload Message chatMessage, Principal principal) {
        
        // Lấy User từ Principal (Cái này do Interceptor đã set từ Token)
        // Nếu Token chứa Email thì cái này trả về Email
        String currentUsername = principal.getName();
        
        // Gán ngược lại vào tin nhắn trước khi lưu
        chatMessage.setSenderId(currentUsername);

        // Lưu vào DB (Giờ thì sender_id đã có dữ liệu rồi nhé)
        Message savedMsg = messageRepository.save(chatMessage);

        // Gửi cho người nhận
        messagingTemplate.convertAndSendToUser(
            chatMessage.getRecipientId(), 
            "/queue/messages", 
            savedMsg
        );
    }

    // API lấy lịch sử tin nhắn giữ nguyên
    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> findChatMessages(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        return ResponseEntity.ok(
            messageRepository.findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
                senderId, recipientId, recipientId, senderId
            )
        );
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id, Principal principal) {
        // Tìm tin nhắn trong DB
        Message msg = messageRepository.findById(id).orElse(null);
        if (msg == null) return ResponseEntity.notFound().build();

        // Kiểm tra chính chủ (Chỉ người gửi mới được xóa)
        if (!msg.getSenderId().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Không phải tin của mầy mà đòi xóa!");
        }

        // Xóa trong Database
        messageRepository.delete(msg);

        // Bắn tín hiệu WebSocket cho cả 2 thằng để cập nhật giao diện ngay lập tức
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DELETE"); 
        payload.put("messageId", id);

        // Gửi cho người nhận (để bên đó tự mất tin nhắn)
        messagingTemplate.convertAndSendToUser(
            msg.getRecipientId(), "/queue/messages", payload
        );
        
        // Gửi lại cho chính mình (để các tab khác hoặc thiết bị khác cũng mất theo)
        messagingTemplate.convertAndSendToUser(
            msg.getSenderId(), "/queue/messages", payload
        );

        return ResponseEntity.ok().build();
    }


    @GetMapping("/conversations")
    public ResponseEntity<List<Message>> getRecentConversations(Principal principal) {
        // Lấy tên người dùng hiện tại
        String currentUser = principal.getName();
        
        // 1. Lấy tất cả tin nhắn liên quan đến mình (mới nhất nằm đầu)
        List<Message> allMessages = messageRepository.findBySenderIdOrRecipientIdOrderByTimestampDesc(currentUser, currentUser);
        
        // 2. Lọc thủ công bằng Java để lấy tin nhắn cuối cùng của từng người
        List<Message> recentChats = new ArrayList<>();
        Set<String> talkedToUsers = new HashSet<>(); // Set dùng để lưu những người đã add vào list rồi

        for (Message msg : allMessages) {
            // Xác định xem "đối phương" là ai
            String partnerId = msg.getSenderId().equals(currentUser) ? msg.getRecipientId() : msg.getSenderId();

            // Nếu chưa gặp người này trong vòng lặp thì đây là tin nhắn mới nhất
            if (!talkedToUsers.contains(partnerId)) {
                recentChats.add(msg);         // Thêm tin nhắn vào danh sách trả về
                talkedToUsers.add(partnerId); // Đánh dấu là đã lấy tin của người này rồi
            }
        }

        return ResponseEntity.ok(recentChats);
    }
}