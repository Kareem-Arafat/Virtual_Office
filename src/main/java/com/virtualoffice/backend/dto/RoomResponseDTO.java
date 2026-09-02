package com.virtualoffice.backend.dto;

import java.time.LocalDateTime;

public class RoomResponseDTO
{
    private Long id;
    private String name;
    private String description;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private int membersCount;
    private boolean currentUserMember;
    private boolean currentUserCreator;

    public RoomResponseDTO(){}

    public RoomResponseDTO(Long id, String name, String description, String createdByUsername, LocalDateTime createdAt, int membersCount, boolean currentUserMember, boolean currentUserCreator)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdByUsername = createdByUsername;
        this.createdAt = createdAt;
        this.membersCount = membersCount;
        this.currentUserMember = currentUserMember;
        this.currentUserCreator = currentUserCreator;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getCreatedByUsername()
    {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername)
    {
        this.createdByUsername = createdByUsername;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public int getMembersCount()
    {
        return membersCount;
    }

    public void setMembersCount(int membersCount)
    {
        this.membersCount = membersCount;
    }

    public boolean isCurrentUserMember()
    {
        return currentUserMember;
    }

    public boolean isCurrentUserCreator()
    {
        return currentUserCreator;
    }
}
