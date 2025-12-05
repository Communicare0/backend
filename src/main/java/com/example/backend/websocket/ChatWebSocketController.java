package com.example.backend.websocket;

import com.example.backend.JwtTokenProvider;
import com.example.backend.dto.request.SendMessageRequest;
import com.example.backend.dto.response.ChatMessageResponse;
import com.example.backend.entity.ChatMessage;
import com.example.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send") // 클라에서 /app/chat.send 로 전송
    public void sendMessage(
            SimpMessageHeaderAccessor headerAccessor,
            @Payload SendMessageRequest request
    ) {
        // 1) 헤더에서 Authorization 직접 꺼내기
        String authorization = headerAccessor.getFirstNativeHeader("Authorization");

        log.info("WS Authorization header = {}", authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authorization.substring(7);
        UUID userId = jwtTokenProvider.extractUserId(token);

        // 2) 기존 서비스 로직 그대로 호출 → DB 저장 + lastChat 갱신
        ChatMessage message = chatMessageService.sendMessage(userId, request);

        // 3) 방별 토픽으로 브로드캐스트
        String destination = "/topic/chat.room." + request.getChatRoomId();
        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);

        messagingTemplate.convertAndSend(destination, response);
    }
}