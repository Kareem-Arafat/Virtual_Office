package com.virtualoffice.backend.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.virtualoffice.backend.service.JwtService;
import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.entity.Room;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.RoomRepository;
import com.virtualoffice.backend.repository.UserRepository;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor
{
    private JwtService jwtService;
    private RoomRepository roomRepository;
    private UserRepository userRepository;


    public WebSocketAuthInterceptor(JwtService jwtService, RoomRepository roomRepository, UserRepository userRepository)
    {
        this.jwtService = jwtService;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }




    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor == null)
        {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand()))
        {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer "))
            {
                String token = authHeader.substring(7);

                String username = jwtService.extractUsername(token);

                if(username == null || !jwtService.isTokenValid(token, username))
                {
                    throw new AccessDeniedException("Invalid authentication token");
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken
                (
                    username,
                    null,
                    null
                );

                accessor.setUser(authentication);
            }
            else
            {
                throw new AccessDeniedException("Authentication is required");
            }
        }

        if(StompCommand.SUBSCRIBE.equals(accessor.getCommand()))
        {
            if(accessor.getUser() == null)
            {
                throw new AccessDeniedException("Authentication is required");
            }

            String username = accessor.getUser().getName();
            String destination = accessor.getDestination();

            if(destination != null && destination.startsWith("/topic/room/"))
            {
                Long roomId = Long.valueOf(destination.substring("/topic/room/".length()));
                requireRoomMembership(username, roomId);
            }

            if(destination != null && destination.startsWith("/topic/notifications/") && !destination.equals("/topic/notifications/" + username))
            {
                throw new AccessDeniedException("You cannot subscribe to another user's notifications");
            }
        }

        if(StompCommand.SEND.equals(accessor.getCommand()))
        {
            if(accessor.getUser() == null)
            {
                throw new AccessDeniedException("Authentication is required");
            }

            String destination = accessor.getDestination();
            if(destination == null || !destination.startsWith("/app/chat/"))
            {
                throw new AccessDeniedException("Direct broker messages are not allowed");
            }

            Long roomId = Long.valueOf(destination.substring("/app/chat/".length()));
            requireRoomMembership(accessor.getUser().getName(), roomId);
        }

        return message;
    }



    

    private void requireRoomMembership(String username, Long roomId)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AccessDeniedException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new AccessDeniedException("Room not found"));

        if(!room.getMembers().contains(user))
        {
            throw new AccessDeniedException("You are not a member of this room");
        }
    }
}
