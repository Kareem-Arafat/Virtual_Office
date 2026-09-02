package com.virtualoffice.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TaskRequestDTO
{
    @NotBlank(message = "Title is required")
    @Size(max = 30, message = "Title cannot exceed 30 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 160, message = "Description cannot exceed 160 characters")
    private String description;


    @NotBlank(message = "Staff ID is required")
    private String assignedToStaffId;



    @NotNull(message = "Deadline is required")
    @FutureOrPresent(message = "Deadline cannot be in the past")
    private LocalDate deadline;



    public TaskRequestDTO(){}

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

    public String getAssignedToStaffId()
    {
        return assignedToStaffId;
    }

    public void setAssignedToStaffId(String assignedToStaffId)
    {
        this.assignedToStaffId = assignedToStaffId;
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
