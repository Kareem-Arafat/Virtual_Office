package com.virtualoffice.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.dto.TaskRequestDTO;
import com.virtualoffice.backend.dto.TaskResponseDTO;
import com.virtualoffice.backend.dto.TaskStatusUpdateDTO;
import com.virtualoffice.backend.dto.TaskUpdateDTO;
import com.virtualoffice.backend.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController
{
    private final TaskService taskService;

    public TaskController(TaskService taskService)
    {
        this.taskService = taskService;
    }



    @PostMapping
    public TaskResponseDTO createTask(@Valid @RequestBody TaskRequestDTO request, Authentication authentication)
    {
        String username = authentication.getName();

        return taskService.createTask(request, username);
    }


    
    @GetMapping
    public List<TaskResponseDTO> getMyTasks(Authentication authentication)
    {
        String username = authentication.getName();

        return taskService.getTasksForUser(username);
    }

    

    /*
     حطينا اي دي عشان احنا هنا هتتعامل مع تاسك معينه ليها اي دي
     عكس اللي فوق كنا لسا هنعمل تاسك ف ملهاش اي دي لسا او نرجع كل التاسكات ل فلان معين ف بردو ملهاش اي دي عشان هنرجع تاسكات بتاعته كلها  
     عكس هنا هنمسح او نعدل تاسكات جديده ف حطينا اي دي
    */
    @PutMapping("/{id}")
    public TaskResponseDTO updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO request, Authentication authentication)
    {
        String username = authentication.getName();

        return taskService.updateTask(id, request, username);
    }




    
    @PatchMapping("/{id}/status")
    public TaskResponseDTO updateTaskStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateDTO request, Authentication authentication)
    {
        String username = authentication.getName();

        return taskService.updateTaskStatus(id, request.getStatus(), username);
    }




    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id, Authentication authentication)
    {
        String username = authentication.getName();

        taskService.deleteTask(id, username);
    }
}