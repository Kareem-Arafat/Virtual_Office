package com.virtualoffice.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.dto.ChatMessageResponseDTO;
import com.virtualoffice.backend.entity.ChatMessage;
import com.virtualoffice.backend.entity.Room;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.ChatMessageRepository;
import com.virtualoffice.backend.repository.RoomRepository;
import com.virtualoffice.backend.repository.UserRepository;

@Service
public class ChatService
{
    private static Set<String> ALLOWED_MEDIA_TYPES = Set.of
    (
        "audio/webm",
        "audio/ogg",
        "audio/mpeg",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "video/mp4",
        "video/webm"
    );

    private ChatMessageRepository chatMessageRepository;
    private RoomRepository roomRepository;
    private UserRepository userRepository;


    
    public ChatService(ChatMessageRepository chatMessageRepository, RoomRepository roomRepository, UserRepository userRepository)
    {
        this.chatMessageRepository = chatMessageRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }




    public ChatMessageResponseDTO sendMessage(Long roomId, String username, String content)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getMembers().contains(user))
        {
            throw new AccessDeniedException("You are not a member of this room");
        }

        String cleanContent = content == null ? "" : content.trim();
        if(cleanContent.isEmpty())
        {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if(cleanContent.length() > 255)
        {
            throw new IllegalArgumentException("Message must be 255 characters or fewer");
        }

        ChatMessage message = new ChatMessage();

        message.setContent(cleanContent);
        message.setSender(user);
        message.setRoom(room);

        chatMessageRepository.save(message);

        return new ChatMessageResponseDTO
        (
            message.getId(),
            message.getContent(),
            message.getSender().getUsername(),
            message.getRoom().getId(),
            message.getTimestamp(),
            message.getMediaType(),
            message.getAudioData()
        );
    }



    public ChatMessageResponseDTO sendMedia(Long roomId, String username, MultipartFile media)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        if(!room.getMembers().contains(user))
        {
            throw new AccessDeniedException("You are not a member of this room");
        }

        if(media == null || media.isEmpty())
        {
            throw new IllegalArgumentException("Media file is empty");
        }

        String mediaType = media.getContentType();
        if(mediaType == null || !ALLOWED_MEDIA_TYPES.contains(mediaType.toLowerCase()))
        {
            throw new IllegalArgumentException("Only audio, image, and video files are allowed");
        }

        mediaType = mediaType.toLowerCase();

        long maximumSize;

        if(mediaType.startsWith("video/"))
        {
            maximumSize = 15L * 1024 * 1024;
        }
        else
        {
            maximumSize = 5L * 1024 * 1024;
        }


        if(media.getSize() > maximumSize)
        {
            if(mediaType.startsWith("video/"))
            {
                throw new IllegalArgumentException("Video must be 15 MB or smaller");
            }
            else
            {
                throw new IllegalArgumentException("Audio and images must be 5 MB or smaller");
            }
        }


        try
        {
            ChatMessage message = new ChatMessage();

            message.setContent("");
            message.setMediaType(mediaType);
            message.setAudioData(media.getBytes());
            message.setSender(user);
            message.setRoom(room);

            chatMessageRepository.save(message);

            return toResponse(message);
        }
        catch(java.io.IOException exception)
        {
            throw new IllegalArgumentException("Could not read the media file");
        }
    }




    public List<ChatMessageResponseDTO> getRoomMessages(Long roomId, String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getMembers().contains(user))
        {
            throw new AccessDeniedException("You are not allowed to view this room chat");
        }

        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);

        List<ChatMessageResponseDTO> response = new ArrayList<>();

        for (ChatMessage message : messages)
        {
            ChatMessageResponseDTO dto = toResponse(message);

            response.add(dto);
        }

        return response;        
    }




    private ChatMessageResponseDTO toResponse(ChatMessage message)
    {
        return new ChatMessageResponseDTO
        (
            message.getId(),
            message.getContent(),
            message.getSender().getUsername(),
            message.getRoom().getId(),
            message.getTimestamp(),
            message.getMediaType(),
            message.getAudioData()
        );
    }
}
