package com.virtualoffice.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.entity.Notification;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.UserRepository;
import com.virtualoffice.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController
{
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    public NotificationController(NotificationService notificationService, UserRepository userRepository)
    {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }


    @GetMapping
    public List<Notification> getNotifications(Authentication authentication)
    {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        return notificationService.getUserNotifications(user);
    }


    @GetMapping("/unread-count")
    public long getUnreadCount(Authentication authentication)
    {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        return notificationService.getUnreadCount(user);
    }


    @PutMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId, Authentication authentication)
    {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        notificationService.markAsRead(notificationId, user);
    }


    @PutMapping("/read-all")
    public void markAllAsRead(Authentication authentication)
    {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new UserNotFoundException("User not found"));
        notificationService.markAllAsRead(user);
    }
}
