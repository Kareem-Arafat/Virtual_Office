package com.virtualoffice.backend.dto;

import java.time.LocalDate;

import com.virtualoffice.backend.entity.Task.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskUpdateDTO
{
    @NotBlank
    @Size(max = 30, message = "Title cannot exceed 30 characters")
    private String title;

    @NotBlank
    @Size(max = 160, message = "Description cannot exceed 160 characters")
    private String description;

    @NotNull
    private TaskStatus status;

    private LocalDate deadline;

    public TaskUpdateDTO(){}

    public TaskUpdateDTO(String title, String description, TaskStatus status, LocalDate deadline)
    {
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
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

    public LocalDate getDeadline()
    {
        return deadline;
    }

    public void setDeadline(LocalDate deadline)
    {
        this.deadline = deadline;
    }
}
