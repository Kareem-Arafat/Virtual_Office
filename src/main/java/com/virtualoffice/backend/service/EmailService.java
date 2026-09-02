package com.virtualoffice.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService
{
    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender)
    {
        this.mailSender = mailSender;
    }



    /* 
        SimpleMailMessage = الجواب 
        JavaMailSender = ساعي البريد 
    */
    @Async
    public void sendSimpleEmail(String to, String subject, String content)
    {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }



    @Async
    public void sendWelcomeEmail(String to, String username, String staffId)
    {
        String subject = "Welcome to Virtual Office";

        String content =
            "Welcome " + username + "!\n\n" +
            "Your account has been created successfully.\n\n" +
            "Your Staff ID is: " + staffId + "\n\n" +
            "You will need this Staff ID when login in.\n\n" +
            "RafiQ Team";

        sendSimpleEmail(to, subject, content);
    }



    public void sendPasswordChangeAlert(String to, String username)
    {
        String subject = "Password Changed";

        String content =
            "Hello " + username + ",\n\n" +
            "Your password has been changed successfully.\n" +
            "You can login with new password";

        sendSimpleEmail(to, subject, content);
    }

    

    @Async
    public void sendRoleChangedEmail(String to, String username, String newRole, String newStaffId)
    {
        String subject = "Your Virtual Office role changed";

        String content =
            "Hello " + username + ",\n\n" +
            "Your role was changed to " + newRole + ".\n\n" +
            "Your new Staff ID is: " + newStaffId + "\n\n" +
            "Use this new Staff ID the next time you log in.";

        sendSimpleEmail(to, subject, content);
    }


    @Async
    public void sendTaskAssignedEmail(String to, String username, String taskTitle)
    {
        String subject = "New task assigned";
        String content = "Hello " + username + ",\n\nNew task assigned: " + taskTitle + ".";

        sendSimpleEmail(to, subject, content);
    }



    @Async
    public void sendRoomAddedEmail(String to, String username, String roomName)
    {
        String subject = "You were added to a room";

        String content =
            "Hello " + username + ",\n\n" +
            "You have been added to the room: " + roomName + ".";

        sendSimpleEmail(to, subject, content);
    }



    public void sendPasswordResetCode(String to, String code)
    {
        String subject = "RafiQ - Password Reset Code";

        String content =
            "Your password reset code is: " +
            code +
            "\n\n" +
            "This code expires in 10 minutes.\n" +
            "Do not share this code with anyone.";

        sendSimpleEmail(to, subject, content);
    }
}
