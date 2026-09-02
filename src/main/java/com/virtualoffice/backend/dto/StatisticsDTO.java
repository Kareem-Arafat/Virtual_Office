package com.virtualoffice.backend.dto;

public class StatisticsDTO
{
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private double completionPercentage;


    public StatisticsDTO(long totalTasks, long completedTasks, long pendingTasks, long inProgressTasks, double completionPercentage)
    {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.completionPercentage = completionPercentage;
    }


    public long getTotalTasks()
    {
        return totalTasks;
    }


    public long getCompletedTasks()
    {
        return completedTasks;
    }


    public long getPendingTasks()
    {
        return pendingTasks;
    }


    public long getInProgressTasks()
    {
        return inProgressTasks;
    }


    public double getCompletionPercentage()
    {
        return completionPercentage;
    }
}