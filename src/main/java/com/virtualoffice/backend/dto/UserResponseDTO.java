package com.virtualoffice.backend.dto;

import com.virtualoffice.backend.entity.User.UserRole;

public class UserResponseDTO 
{
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private String bio;
    private String phone;
    private String staffId;

    public UserResponseDTO(Long id, String username, String staffId, String email, UserRole role, String bio, String phone) 
    {
        this.id = id;
        this.username = username;
        this.staffId = staffId;
        this.email = email;
        this.role = role;
        this.bio = bio;
        this.phone = phone;
    }

    public Long getId() 
    {
        return id;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public String getUsername() 
    {
        return username;
    }

    public void setUsername(String username) 
    {
        this.username = username;
    }

    public String getEmail() 
    {
        return email;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    public UserRole getRole()
    {
        return role;
    }

    public void setRole(UserRole role)
    {
        this.role = role;
    }

    public String getBio()
    {
        return bio;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getStaffId()
    {
        return staffId;
    }

    public void setStaffId(String staffId)
    {
        this.staffId = staffId;
    }
}