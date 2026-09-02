package com.virtualoffice.backend.service;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.Exception.TaskNotFoundException;
import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.dto.TaskRequestDTO;
import com.virtualoffice.backend.dto.TaskResponseDTO;
import com.virtualoffice.backend.dto.TaskUpdateDTO;
import com.virtualoffice.backend.entity.Task;
import com.virtualoffice.backend.entity.Task.TaskStatus;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.TaskRepository;
import com.virtualoffice.backend.repository.UserRepository;

@Service
public class TaskService
{
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, NotificationService notificationService, EmailService emailService)
    {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }




    // ======================================================
    // Manager    -> anyone
    // TeamLeader -> own employees only
    // Developer  -> not allow
    // ======================================================
    public TaskResponseDTO createTask(TaskRequestDTO request, String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() ->new UserNotFoundException("User not found"));

        if(user.getRole() != User.UserRole.MANAGER && user.getRole() != User.UserRole.TEAM_LEADER)
        {
            throw new AccessDeniedException("You do not have permission to assign tasks");
        }

        String staffId = request.getAssignedToStaffId().trim().toUpperCase();
        User assignedUser = userRepository.findByStaffId(staffId).orElseThrow(() -> new UserNotFoundException("Staff ID not found"));

        if(user.getRole() == User.UserRole.TEAM_LEADER)
        {
            if(assignedUser.getTeamLeader() == null ||!assignedUser.getTeamLeader().getId().equals(user.getId()))
            {
                throw new AccessDeniedException("You can assign tasks only to your employees");
            }
        }

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setStatus(TaskStatus.TODO);
        task.setUser(assignedUser);

        taskRepository.save(task);

        notificationService.sendNotification(assignedUser, "New task assigned to you: " + task.getTitle());
        emailService.sendTaskAssignedEmail(assignedUser.getEmail(), assignedUser.getUsername(), task.getTitle());

        return new TaskResponseDTO
        (
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getCreatedAt(),
            task.getDeadline(),
            assignedUser.getUsername(),
            assignedUser.getStaffId()
        );
    }





    public List<TaskResponseDTO> getTasksForUser(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() ->new UserNotFoundException("User not found"));

        List<Task> tasks;


        if(user.getRole() == User.UserRole.MANAGER)
        {
            // Manager sees all task
            tasks = taskRepository.findAll();
        }
        else if(user.getRole() == User.UserRole.TEAM_LEADER)
        {
            // Team Leader sees tasks  for his employees
            tasks = taskRepository.findByUserTeamLeaderId(user.getId());
        }
        else
        {
            // Developer sees only his own task
            tasks = taskRepository.findByUserId(user.getId());
        }

        
        List<TaskResponseDTO> response = new ArrayList<>();
        for (Task task : tasks)
        {
            TaskResponseDTO dto = new TaskResponseDTO
            (
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getDeadline(),
                task.getUser().getUsername(),
                task.getUser().getStaffId()
            );

            response.add(dto);
        }

        return response;
    }




    // ======================================================
    // Manager:
    // any task
    //
    // Team Leader:
    // own employees only
    //
    // Developer:
    // forbidden
    // ======================================================
    private Task getTaskIfCanManage(Long taskId, String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        Task task = taskRepository.findById(taskId).orElseThrow(() ->new TaskNotFoundException("Task not found"));

        // Manager can manage every task
        if(user.getRole() == User.UserRole.MANAGER)
        {
            return task;
        }


        // Team Leader can manage own employees' task
        if(user.getRole() == User.UserRole.TEAM_LEADER)
        {
            User employee = task.getUser();

            if(employee.getTeamLeader() != null && employee.getTeamLeader().getId().equals(user.getId()))
            {
                return task;
            }

            throw new AccessDeniedException("You cannot manage this employee's task");
        }

        throw new AccessDeniedException("You do not have permission to manage this task");
    }






    public TaskResponseDTO updateTask(Long taskId, TaskUpdateDTO request, String username)
    {
        Task task = getTaskIfCanManage(taskId, username);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());

        if(request.getDeadline() != null)
        {
            task.setDeadline(request.getDeadline());
        }

        taskRepository.save(task);

        return new TaskResponseDTO
        (
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getCreatedAt(),
            task.getDeadline(),
            task.getUser().getUsername(),
            task.getUser().getStaffId()
        );
    }




    // ======================================================
    // Manager:
    // any task
    //
    // Team Leader:
    // own employees
    //
    // Developer:
    // his own task
    // ======================================================
    private Task getTaskIfCanChangeStatus(Long taskId, String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException("Task not found"));


        if(user.getRole() == User.UserRole.MANAGER)
        {
            return task;
        }


        if(user.getRole() == User.UserRole.TEAM_LEADER)
        {
            User employee = task.getUser();

            if(employee.getTeamLeader() != null && employee.getTeamLeader().getId().equals(user.getId()))
            {
                return task;
            }
        }


        if(user.getRole() == User.UserRole.DEVELOPER && task.getUser().getId().equals(user.getId()))
        {
            return task;
        }

        throw new AccessDeniedException("You cannot change this task status");
    }





    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatus status, String username)
    {
        Task task = getTaskIfCanChangeStatus(taskId, username);

        task.setStatus(status);

        taskRepository.save(task);

        return new TaskResponseDTO
        (
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getCreatedAt(),
            task.getDeadline(),
            task.getUser().getUsername(),
            task.getUser().getStaffId()
        );
    }



    
    public void deleteTask(Long taskId, String username)
    {
        Task task = getTaskIfCanManage(taskId, username);

        taskRepository.delete(task);
    }
}
