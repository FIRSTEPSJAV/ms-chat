package com.tesis.mschat.controller;

import com.tesis.mschat.entity.ChatMessage;
import com.tesis.mschat.entity.User;
import com.tesis.mschat.entity.ChatRoom;
import com.tesis.mschat.repository.UserRepository;
import com.tesis.mschat.repository.ChatRoomRepository;
import com.tesis.mschat.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate, UserRepository userRepository, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
    }


    // /app/sendMessage
    @MessageMapping("/sendMessage")
    public void sendMessage(ChatMessage message) {
        String sender = message.getSender();
        String recipient = message.getRecipient();
        String roomKey1 = sender + "-" + recipient;
        String roomKey2 = recipient + "-" + sender;

        // Handle both room directions
        ChatRoom chatRoom1 = chatRoomRepository.findByRoomKey(roomKey1).orElseGet(() -> {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setRoomKey(roomKey1);
            newRoom.setSender(sender);
            newRoom.setRecipient(recipient);
            return chatRoomRepository.save(newRoom);
        });
        ChatRoom chatRoom2 = chatRoomRepository.findByRoomKey(roomKey2).orElseGet(() -> {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setRoomKey(roomKey2);
            newRoom.setSender(recipient);
            newRoom.setRecipient(sender);
            return chatRoomRepository.save(newRoom);
        });

        ChatMessage msg1 = new ChatMessage(null, message.getContent(), sender, recipient, chatRoom1.getId(), LocalDateTime.now());
        ChatMessage msg2 = new ChatMessage(null, message.getContent(), sender, recipient, chatRoom2.getId(), LocalDateTime.now());
        chatMessageRepository.save(msg1);
        chatMessageRepository.save(msg2);

        // Send to each user for their respective room only
        messagingTemplate.convertAndSendToUser(
            sender,
            "/queue/room/" + chatRoom1.getId(),
            msg1
        );
        messagingTemplate.convertAndSendToUser(
            recipient,
            "/queue/room/" + chatRoom2.getId(),
            msg2
        );
    }

    @GetMapping("/api/room/{chatRoomId}/history")
    @ResponseBody
    public List<ChatMessage> getRoomHistory(@PathVariable String chatRoomId) {
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }

    @GetMapping("/api/room/{user1}/{user2}")
    @ResponseBody
    public String getOrCreateRoomId(@PathVariable String user1, @PathVariable String user2) {
        String roomKey = user1 + "-" + user2;
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findByRoomKey(roomKey);
        ChatRoom chatRoom = chatRoomOpt.orElseGet(() -> {
            ChatRoom newRoom = new ChatRoom();
            newRoom.setRoomKey(roomKey);
            newRoom.setSender(user1);
            newRoom.setRecipient(user2);
            return chatRoomRepository.save(newRoom);
        });
        return chatRoom.getId();
    }

    @GetMapping("/chat")
    public String chat(){
        return "chat";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = sha.getUser();
        if (user != null) {
            // Update user status in DB
            userRepository.findByUsername(user.getName())
                .ifPresentOrElse(
                    u -> { u.setStatus(User.Status.CONNECTED); userRepository.save(u); },
                    () -> userRepository.save(new User(null, user.getName(), User.Status.CONNECTED))
                );
            broadcastUserList();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = sha.getUser();
        if (user != null) {
            userRepository.findByUsername(user.getName())
                .ifPresent(u -> { u.setStatus(User.Status.DISCONNECTED); userRepository.save(u); });
            broadcastUserList();
        }
    }

    private void broadcastUserList() {
        // Send all users with their status
        messagingTemplate.convertAndSend("/topic/users", userRepository.findAll());
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
