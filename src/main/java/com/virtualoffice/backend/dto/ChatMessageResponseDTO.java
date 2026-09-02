package com.virtualoffice.backend.dto;

import java.time.LocalDateTime;

public class ChatMessageResponseDTO
{
    private Long id;
    private String content;
    private String senderUsername;
    private Long roomId;
    private LocalDateTime timestamp;
    private String mediaType;
    private byte[] audioData;

    public ChatMessageResponseDTO(Long id, String content, String senderUsername, Long roomId, LocalDateTime timestamp, String mediaType, byte[] audioData)
    {
        this.id = id;
        this.content = content;
        this.senderUsername = senderUsername;
        this.roomId = roomId;
        this.timestamp = timestamp;
        this.mediaType = mediaType;
        this.audioData = audioData;
    }

    public Long getId()
    {
        return id;
    }

    public String getContent()
    {
        return content;
    }

    public String getSenderUsername()
    {
        return senderUsername;
    }

    public Long getRoomId()
    {
        return roomId;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public String getMediaType()
    {
        return mediaType;
    }

    public byte[] getAudioData()
    {
        return audioData;
    }
}
