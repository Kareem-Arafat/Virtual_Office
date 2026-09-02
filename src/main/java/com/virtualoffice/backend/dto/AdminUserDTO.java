package com.virtualoffice.backend.dto;

import com.virtualoffice.backend.entity.User.UserRole;

public class AdminUserDTO
{
    private Long id;
    private String username;
    private String staffId;
    private String email;
    private UserRole role;
    private String teamLeaderUsername;


    public AdminUserDTO(Long id, String username, String staffId, String email, UserRole role, String teamLeaderUsername)
    {
        this.id = id;
        this.username = username;
        this.staffId = staffId;
        this.email = email;
        this.role = role;
        this.teamLeaderUsername = teamLeaderUsername;
    }


    public Long getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getStaffId()
    {
        return staffId;
    }

    public String getEmail()
    {
        return email;
    }

    public UserRole getRole()
    {
        return role;
    }

    public String getTeamLeaderUsername()
    {
        return teamLeaderUsername;
    }
}
