package com.virtualoffice.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.virtualoffice.backend.dto.PasswordChangeRequestDTO;
import com.virtualoffice.backend.dto.ProfileUpdateDTO;
import com.virtualoffice.backend.dto.AdminUserDTO;
import com.virtualoffice.backend.dto.UserResponseDTO;
import com.virtualoffice.backend.service.UserService;

import com.virtualoffice.backend.entity.User;
import jakarta.validation.Valid;

@RestController
public class UserController
{

    private final UserService userService;


    public UserController(UserService userService)
    {
        this.userService = userService;
    }



    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(Authentication authentication)
    {
        String username = authentication.getName();

        return userService.getUserByUsername(username);
    }


    @GetMapping("/api/users/visible")
    public List<AdminUserDTO> getVisibleUsers(Authentication authentication)
    {
        return userService.getVisibleUsers(authentication.getName());
    }



    @PutMapping("/api/users/profile")
    public UserResponseDTO updateProfile(@Valid @RequestBody ProfileUpdateDTO request, Authentication authentication)
    {
        String username = authentication.getName();

        return userService.updateProfile(username, request);
    }



    @PutMapping("/api/users/change-password")
    public void changePassword(@Valid @RequestBody PasswordChangeRequestDTO request, Authentication authentication)
    {
        String username = authentication.getName();

        userService.changePassword(username, request);
    }


    @DeleteMapping("/api/users/me")
    public void deleteMyAccount(Authentication authentication)
    {
        String username = authentication.getName();

        userService.deleteAccount(username);
    }



    @PutMapping("/api/users/{id}/role")
    public void updateUserRole(@PathVariable Long id, @RequestParam User.UserRole newRole, Authentication authentication)
    {
        String managerUsername = authentication.getName();

        userService.updateUserRole(id, newRole, managerUsername);
    }

    @PostMapping("/api/team/members")
    public void addTeamMember(@RequestParam String staffId, Authentication authentication)
    {
        userService.addEmployeeToMyTeam(authentication.getName(), staffId);
    }

    @DeleteMapping("/api/team/members")
    public void removeTeamMember(@RequestParam String staffId, Authentication authentication)
    {
        userService.removeEmployeeFromMyTeam(authentication.getName(), staffId);
    }
}
