package com.virtualoffice.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.dto.AdminUserDTO;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.entity.User.UserRole;
import com.virtualoffice.backend.repository.UserRepository;

@Service
public class AdminService
{
    private UserRepository userRepository;
    private UserService userService;


    public AdminService(UserRepository userRepository, UserService userService)
    {
        this.userRepository = userRepository;
        this.userService = userService;
    }




    private User getManager(String username)
    {
        User manager = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));


        if (manager.getRole() != UserRole.MANAGER)
        {
            throw new AccessDeniedException("Only managers can access the admin panel");
        }

        return manager;
    }




    public List<AdminUserDTO> getAllUsers(String currentUsername)
    {
        getManager(currentUsername);


        List<User> users = userRepository.findAll();


        List<AdminUserDTO> response = new ArrayList<>();

        for (User user : users)
        {
            AdminUserDTO dto = new AdminUserDTO
            (
                user.getId(),
                user.getUsername(),
                user.getStaffId(),
                user.getEmail(),
                user.getRole(),
                user.getTeamLeader() == null ? null : user.getTeamLeader().getUsername()
            );

            response.add(dto);
        }

        return response;
    }




    public AdminUserDTO changeUserRole(Long userId, UserRole newRole, String currentUsername)
    {
        User manager = getManager(currentUsername);

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));


        if (user.getId().equals(manager.getId()))
        {
            throw new AccessDeniedException("You cannot change your own manager role");
        }

        userService.updateUserRole(userId, newRole, currentUsername);

        user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        return new AdminUserDTO
        (
            user.getId(),
            user.getUsername(),
            user.getStaffId(),
            user.getEmail(),
            user.getRole(),
            user.getTeamLeader() == null ? null : user.getTeamLeader().getUsername()
        );
    }




    public void deleteUser(Long userId, String currentUsername)
    {
        User manager = getManager(currentUsername);

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getId().equals(manager.getId()))
        {
            throw new AccessDeniedException("You cannot delete your own account from the admin panel");
        }


        userService.deleteAccount(user.getUsername());
    }
}
