package com.api.group9.service;

import com.api.group9.model.Message;
import com.api.group9.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Xử lý lưu và gửi tin nhắn mới
    public void saveAndSendMessage(Message chatMessage, String senderId) {
        chatMessage.setSenderId(senderId);
        Message savedMsg = messageRepository.save(chatMessage);

        messagingTemplate.convertAndSendToUser(
            chatMessage.getRecipientId(),
            "/queue/messages",
            savedMsg
        );
    }

    // Lấy lịch sử tin nhắn
    public List<Message> getChatHistory(String senderId, String recipientId) {
        return messageRepository.findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
                senderId, recipientId, recipientId, senderId
        );
    }

    // Xử lý xóa tin nhắn
    public boolean deleteMessage(Long id, String currentUsername) {
        Message msg = messageRepository.findById(id).orElse(null);
        
        // Kiểm tra tồn tại và quyền sở hữu
        if (msg == null || !msg.getSenderId().equals(currentUsername)) {
            return false;
        }

        messageRepository.delete(msg);

        // Logic gửi thông báo xóa qua WebSocket
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DELETE");
        payload.put("messageId", id);

        messagingTemplate.convertAndSendToUser(msg.getRecipientId(), "/queue/messages", payload);
        messagingTemplate.convertAndSendToUser(msg.getSenderId(), "/queue/messages", payload);
        
        return true;
    }

    public List<Message> getRecentConversations(String currentUser) {
        List<Message> allMessages = messageRepository.findBySenderIdOrRecipientIdOrderByTimestampDesc(currentUser, currentUser);
        
        List<Message> recentChats = new ArrayList<>();
        Set<String> talkedToUsers = new HashSet<>();

        for (Message msg : allMessages) {
            String partnerId = msg.getSenderId().equals(currentUser) ? msg.getRecipientId() : msg.getSenderId();
            if (!talkedToUsers.contains(partnerId)) {
                recentChats.add(msg);
                talkedToUsers.add(partnerId);
            }
        }
        return recentChats;
    }
}