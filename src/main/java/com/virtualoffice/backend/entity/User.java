package com.virtualoffice.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")

public class User 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private String phone;
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserRole role;


    public enum UserRole
    {
        DEVELOPER,
        TEAM_LEADER,
        MANAGER
    }

    @Column(length = 1000)
    private String bio;

    /*  
        mappedBy = "user" → specifies that the relationship is managed by the "user" field in Task
        من الاخر يعني لما تيجي تمسح اليوزر ده يبقي اي تاسكات مرتبطه بيه امسحها بردو
    */
    // CascadeType.REMOVE → when a User is deleted, all related Tasks are deleted automatically
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Task> tasks;


    private String tempCode;

    private LocalDateTime tempCodeExpiresAt;

    /* ============================================================================================ */
    @ManyToOne
    @JoinColumn(name = "team_leader_id") // كل موظف ليه اي دي بتاع التيم ليدر بتاعه
    private User teamLeader;

    @OneToMany(mappedBy = "teamLeader")
    private List<User> teamMembers; // عشان نجيب كل الموظفين اللي عند التيم ليدر ده

    @ManyToMany(mappedBy = "members")
    private Set<Room> rooms = new HashSet<>(); // يجيب كل الرو اللي العضو ده فيها
    /* ============================================================================================ */


    /* ============================================================================================ */
    @OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE)
    private List<Post> posts = new ArrayList<>();

    @ManyToMany(mappedBy = "likedByUsers")
    private Set<Post> likedPosts = new HashSet<>();
    /* ============================================================================================ */


    // ==================== STAFF ID ====================
    @Column(unique = true)
    private String staffId;
    // ===========================================================


    public User() {}



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role){ this.role = role; }

    public String getBio(){ return bio; }
    public void setBio(String bio){ this.bio = bio; }

    public User getTeamLeader( ){ return teamLeader; }
    public void setTeamLeader(User teamLeader) { this.teamLeader = teamLeader; }

    public void setTeamMembers(List<User> teamMembers){ this.teamMembers = teamMembers; }
    public List<User> getTeamMembers(){ return teamMembers; }

    public Set<Room> getRooms(){ return rooms; }
    public void setRooms(Set<Room> rooms){ this.rooms = rooms; }

    public List<Post> getPosts(){ return posts; }
    public void setPosts(List<Post> posts){ this.posts = posts; }

    public Set<Post> getLikedPosts(){ return likedPosts; }
    public void setLikedPosts(Set<Post> likedPosts){ this.likedPosts = likedPosts; }

    public String getTempCode(){ return tempCode; }
    public void setTempCode(String tempCode){ this.tempCode = tempCode; }

    public LocalDateTime getTempCodeExpiresAt(){ return tempCodeExpiresAt; }
    public void setTempCodeExpiresAt(LocalDateTime tempCodeExpiresAt){ this.tempCodeExpiresAt = tempCodeExpiresAt; }

    public String getStaffId(){ return staffId; }
    public void setStaffId(String staffId){ this.staffId = staffId; }
}