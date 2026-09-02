package com.virtualoffice.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING) // ديه بتقول للداتابيز ازاي تخزن الاينيم عشان هي متعرفش ف بنقولها تخزنه ك سترينج
    private TaskStatus status;
    public enum TaskStatus
    {
        TODO,
        IN_PROGRESS,
        DONE
    }

    private LocalDateTime createdAt; // امتي تاسك تم انشاؤه
    private LocalDate deadline; // امتي التاسك هينتهي

    @ManyToOne
    @JoinColumn(name = "userId") // Foreign key
    private User user; // يخزن اليوزر اللي عمل التاسك ده عشان نقدر نجيب كل التاسكات بتاعه
    /*
        مكتبناش 
        private Long userId;
       لأن 
       JPA 
       بتتعامل مع 
       Objects 
       مش أرقام.
    */



    @PrePersist // ده اوتوماتيك لكل تاسك جديد تخزنه ف الداتابيز لازم الفانكشن ديه تحصل اللي هو تخزن الوقت اللي اتعملت فيه
    public void onCreate()
    {
        createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "assigned_by")
    private User assignedBy;



    public Task() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) {this.deadline = deadline; }
}