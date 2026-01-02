package com.api.group9.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.api.group9.model.Message;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
        String senderId1, String recipientId1, String senderId2, String recipientId2
    );

    List<Message> findBySenderIdOrRecipientIdOrderByTimestampDesc(String senderId, String recipientId);
}