package com.virtualoffice.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoomRequestDTO 
{
    @NotBlank(message = "Room name is required")
    @Size(max = 80, message = "Room name cannot exceed 80 characters")
    private String name;

    @Size(max = 255, message = "Room description cannot exceed 255 characters")
    private String description;
    
    public RoomRequestDTO() {}

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
}
