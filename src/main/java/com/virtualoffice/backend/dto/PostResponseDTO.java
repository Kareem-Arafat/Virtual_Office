package com.virtualoffice.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponseDTO
{
    private Long id;
    private String content;
    private String imageUrl;
    private String authorUsername;
    private LocalDateTime createdAt;
    private int likesCount;
    private boolean likedByMe;
    private boolean canDelete;
    private List<CommentResponseDTO> comments;


    public PostResponseDTO(Long id, String content,String imageUrl, String authorUsername, LocalDateTime createdAt, int likesCount, boolean likedByMe, boolean canDelete, List<CommentResponseDTO> comments)
    {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.authorUsername = authorUsername;
        this.createdAt = createdAt;
        this.likesCount = likesCount;
        this.likedByMe = likedByMe;
        this.canDelete = canDelete;
        this.comments = comments;
    }


    public Long getId()
    {
        return id;
    }

    public String getContent()
    {
        return content;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public String getAuthorUsername()
    {
        return authorUsername;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public int getLikesCount()
    {
        return likesCount;
    }

    public boolean isLikedByMe()
    {
        return likedByMe;
    }

    public boolean isCanDelete()
    {
        return canDelete;
    }

    public List<CommentResponseDTO> getComments()
    {
        return comments;
    }
}