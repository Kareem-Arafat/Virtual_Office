package com.virtualoffice.backend.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class Room
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy; // بيربط اليوزر بالروم اللي عملها 


    /*
        هتعمل جدول اسمه ف الداتابيز اسمه
        room_members
        عندك ال 
        joinColumns
        يعني انا بتكلم عن الكلاس اللي انا واقف فيه حالا اللي هو 
        room 
        و عندك 
        @JoinColumn(name = "room_id")
        يعني اعمل عمود بالاسم ده بيشاور علي اي دي بتاع الروم عشا انا بتكلم عليه حاليا 

        inverseJoinColumns
        يعني العمود اللي يخص الطرف التاني اللي هو يوزر هنعمل عمود يبص عليه 
        طب هو عرف منين اني قصدي يوزر ؟
        Set<User> members = new HashSet<>();
        ال اوبجكت اللي مكتوب نوعه يوزر 

        كدا انت عملت جدول فيه 2 عمودين و العمودين فوريجن كي اصلا عشان تربط اليوزر بالروم
    
    */
    @ManyToMany
    @JoinTable(name = "room_members", joinColumns = @JoinColumn(name = "room_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    @PrePersist
    public void onCreate()
    {
        createdAt = LocalDateTime.now();
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public User getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(User createdBy)
    {
        this.createdBy = createdBy;
    }

    public Set<User> getMembers()
    {
        return members;
    }

    public void setMembers(Set<User> members)
    {
        this.members = members;
    }
}