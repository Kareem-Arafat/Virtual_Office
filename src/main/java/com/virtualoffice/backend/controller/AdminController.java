package com.virtualoffice.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.dto.AdminUserDTO;
import com.virtualoffice.backend.entity.User.UserRole;
import com.virtualoffice.backend.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController
{
    private AdminService adminService;


    public AdminController(AdminService adminService)
    {
        this.adminService = adminService;
    }




    @GetMapping("/users")
    public List<AdminUserDTO> getAllUsers(Authentication authentication)
    {
        String username = authentication.getName();

        return adminService.getAllUsers(username);
    }




    @PutMapping("/users/{userId}/role")
    public AdminUserDTO changeUserRole(@PathVariable Long userId, @RequestParam UserRole role, Authentication authentication)
    {
        String username = authentication.getName();

        return adminService.changeUserRole
        (
            userId,
            role,
            username
        );
    }




    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable Long userId, Authentication authentication)
    {
        String username =authentication.getName();

        adminService.deleteUser(userId,username);
    }
}