package com.tesis.mschat.repository;

import com.tesis.mschat.entity.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    Optional<ChatRoom> findBySenderAndRecipient(String sender, String recipient);
    Optional<ChatRoom> findBySenderAndRecipientOrSenderAndRecipient(String sender1, String recipient1, String sender2, String recipient2);
    Optional<ChatRoom> findByRoomKey(String roomKey);
}
