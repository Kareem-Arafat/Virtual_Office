package com.virtualoffice.backend.dto;

import com.virtualoffice.backend.entity.Task.TaskStatus;

import jakarta.validation.constraints.NotNull;

public class TaskStatusUpdateDTO
{
    @NotNull
    private TaskStatus status;


    public TaskStatus getStatus()
    {
        return status;
    }

    public void setStatus(TaskStatus status)
    {
        this.status = status;
    }
}