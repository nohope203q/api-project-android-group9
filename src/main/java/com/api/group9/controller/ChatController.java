package com.api.group9.controller;

import com.api.group9.model.Message;
import com.api.group9.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService; 

    @MessageMapping("/chat")
    public void processMessage(@Payload Message chatMessage, Principal principal) {
        chatService.saveAndSendMessage(chatMessage, principal.getName());
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> findChatMessages(
            @PathVariable String senderId,
            @PathVariable String recipientId) {
        return ResponseEntity.ok(chatService.getChatHistory(senderId, recipientId));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id, Principal principal) {
        boolean isDeleted = chatService.deleteMessage(id, principal.getName());
        
        if (!isDeleted) {
            return ResponseEntity.status(403).body("Không tìm thấy tin nhắn hoặc bạn không có quyền xóa!");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<Message>> getRecentConversations(Principal principal) {
        return ResponseEntity.ok(chatService.getRecentConversations(principal.getName()));
    }
}