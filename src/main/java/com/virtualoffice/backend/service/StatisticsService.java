package com.virtualoffice.backend.service;

import org.springframework.stereotype.Service;

import com.virtualoffice.backend.dto.StatisticsDTO;
import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.entity.Task.TaskStatus;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.TaskRepository;
import com.virtualoffice.backend.repository.UserRepository;

@Service
public class StatisticsService
{
    private TaskRepository taskRepository;
    private UserRepository userRepository;


    public StatisticsService(TaskRepository taskRepository, UserRepository userRepository)
    {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }





    public StatisticsDTO getOfficeStats(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));


        if (user.getRole() != User.UserRole.MANAGER)
        {
            throw new AccessDeniedException("Only managers can view office statistics");
        }


        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long pendingTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        double completionPercentage = 0;


        if (totalTasks > 0)
        {
            completionPercentage = ((double) completedTasks / totalTasks) * 100;
        }


        return new StatisticsDTO
        (
            totalTasks,
            completedTasks,
            pendingTasks,
            inProgressTasks,
            completionPercentage
        );
    }






    public StatisticsDTO getMyStats(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));


        Long userId = user.getId();

        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countByUserIdAndStatus(userId,TaskStatus.DONE);
        long pendingTasks = taskRepository.countByUserIdAndStatus(userId,TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByUserIdAndStatus(userId,TaskStatus.IN_PROGRESS);
        double completionPercentage = 0;


        if (totalTasks > 0)
        {
            completionPercentage = ((double) completedTasks / totalTasks)* 100;
        }


        return new StatisticsDTO
        (
            totalTasks,
            completedTasks,
            pendingTasks,
            inProgressTasks,
            completionPercentage
        );
    }
}
