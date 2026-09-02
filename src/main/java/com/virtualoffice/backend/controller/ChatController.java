package com.virtualoffice.backend.controller;

import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.virtualoffice.backend.dto.ChatMessageResponseDTO;
import com.virtualoffice.backend.entity.ChatMessage;
import com.virtualoffice.backend.service.ChatService;

@RestController
public class ChatController
{
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate)
    {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }


    // يبعت رسالة جديدة لايف
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageResponseDTO  sendMessage(@DestinationVariable Long roomId, ChatMessage message, Authentication authentication)
    {
        String username = authentication.getName();

        return chatService.sendMessage(roomId, username, message.getContent());
    }



    // يجيب الرسائل القديمة
    @GetMapping("/api/rooms/{roomId}/messages")
    public List<ChatMessageResponseDTO> getRoomMessages(@PathVariable Long roomId, Authentication authentication)
    {
        String username = authentication.getName();

        return chatService.getRoomMessages(roomId, username );
    }



    @PostMapping("/api/rooms/{roomId}/messages")
    public ChatMessageResponseDTO sendTextMessage(@PathVariable Long roomId, @RequestBody ChatMessage request, Authentication authentication)
    {
        ChatMessageResponseDTO message = chatService.sendMessage(roomId, authentication.getName(), request.getContent());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
        return message;
    }


    
    @PostMapping("/api/rooms/{roomId}/media")
    public ChatMessageResponseDTO sendMedia(@PathVariable Long roomId, @RequestParam("media") MultipartFile media, Authentication authentication)
    {
        ChatMessageResponseDTO message = chatService.sendMedia(roomId, authentication.getName(), media);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
        return message;
    }
}
