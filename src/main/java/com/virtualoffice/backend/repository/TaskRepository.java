package com.virtualoffice.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualoffice.backend.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long>
{
    List<Task> findByUserId(Long userId); // يرجع تاسكات بتاع اليوزر اللي اي دي بتاعه كذا 


    long countByStatus(Task.TaskStatus status); // دي بتعد كل التاسكات في السيستم اللي حالتها كذا

    long countByUserId(Long userId); // دي بتعد كل التاسكات الخاصة بيوزر معين مهما كانت حالتها

    long countByUserIdAndStatus(Long userId, Task.TaskStatus status); // ديخ بتعد التاسكات الخاصة باليوزر ده واللي حالتها كذا

    List<Task> findByUserTeamLeaderId(Long teamLeaderId);

    void deleteByUserId(Long userId);
}