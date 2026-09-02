package com.virtualoffice.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.virtualoffice.backend.entity.Task.TaskStatus;

public class TaskResponseDTO
{
    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private LocalDateTime createdAt;

    private LocalDate deadline;

    private String assignedToUsername;

    private String assignedToStaffId;

    public TaskResponseDTO(){}

    public TaskResponseDTO(Long id, String title, String description, TaskStatus status, LocalDateTime createdAt, LocalDate deadline)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.deadline = deadline;
    }

    public TaskResponseDTO(Long id, String title, String description, TaskStatus status, LocalDateTime createdAt, LocalDate deadline, String assignedToUsername, String assignedToStaffId)
    {
        this(id, title, description, status, createdAt, deadline);
        this.assignedToUsername = assignedToUsername;
        this.assignedToStaffId = assignedToStaffId;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public TaskStatus getStatus()
    {
        return status;
    }

    public void setStatus(TaskStatus status)
    {
        this.status = status;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt)
    {
        this.createdAt = createdAt;
    }

    public LocalDate getDeadline()
    {
        return deadline;
    }

    public void setDeadline(LocalDate deadline)
    {
        this.deadline = deadline;
    }

    public String getAssignedToUsername()
    {
        return assignedToUsername;
    }

    public void setAssignedToUsername(String assignedToUsername)
    {
        this.assignedToUsername = assignedToUsername;
    }

    public String getAssignedToStaffId()
    {
        return assignedToStaffId;
    }

    public void setAssignedToStaffId(String assignedToStaffId)
    {
        this.assignedToStaffId = assignedToStaffId;
    }
}
