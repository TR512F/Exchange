package com.bubnov.v5.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessageToUser(String username, String message) {
        SimpUser user = simpUserRegistry.getUser(username);
        if (user != null) {
            messagingTemplate.convertAndSendToUser(username, "/queue/messages", message);
        } else {
            System.out.println("User is not connected: " + username);
        }
    }
}
