package com.virtualoffice.backend.dto;

import java.time.LocalDateTime;

public class CommentResponseDTO
{
    private Long id;
    private String text;
    private String username;
    private LocalDateTime createdAt;
    private boolean canDelete;


    public CommentResponseDTO(Long id, String text, String username, LocalDateTime createdAt, boolean canDelete)
    {
        this.id = id;
        this.text = text;
        this.username = username;
        this.createdAt = createdAt;
        this.canDelete = canDelete;
    }


    public Long getId()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }

    public String getUsername()
    {
        return username;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }
}