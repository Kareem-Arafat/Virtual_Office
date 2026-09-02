package com.virtualoffice.backend.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.virtualoffice.backend.entity.Notification;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.repository.NotificationRepository;

@Service
public class NotificationService
{
    private NotificationRepository notificationRepository;
    private SimpMessagingTemplate messagingTemplate;


    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate)
    {
        this.notificationRepository = notificationRepository;

        this.messagingTemplate = messagingTemplate;
    }





    public Notification sendNotification(User recipient, String message)
    {
        Notification notification = new Notification();


        notification.setRecipient(recipient);

        notification.setMessage(message);

        notification = notificationRepository.save(notification);

        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getUsername(), notification);


        return notification;
    }





    public List<Notification> getUserNotifications(User user)
    {
        return notificationRepository.findByRecipientIdOrderByTimestampDesc(user.getId());
    }





    public long getUnreadCount(User user)
    {
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }




    public void markAsRead(Long notificationId, User user)
    {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("Notification not found"));


        if (!notification.getRecipient().getId().equals(user.getId()))
        {
            throw new AccessDeniedException("You cannot read this notification");
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }


    public void markAllAsRead(User user)
    {
        List<Notification> userNotifications = notificationRepository.findByRecipientIdOrderByTimestampDesc(user.getId());

        for(Notification notification : userNotifications)
        {
            if(!notification.isRead())
            {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }
    }
}
