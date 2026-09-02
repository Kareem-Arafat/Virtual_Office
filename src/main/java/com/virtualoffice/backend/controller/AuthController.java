package com.virtualoffice.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.dto.ForgotPasswordRequestDTO;
import com.virtualoffice.backend.dto.LoginRequestDTO;
import com.virtualoffice.backend.dto.LoginResponseDTO;
import com.virtualoffice.backend.dto.RegisterRequestDTO;
import com.virtualoffice.backend.dto.ResetPasswordDTO;
import com.virtualoffice.backend.dto.UserResponseDTO;
import com.virtualoffice.backend.dto.VerifyResetCodeDTO;
import com.virtualoffice.backend.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController
{
    private final UserService userService;


    public AuthController(UserService userService)
    {
        this.userService = userService;
    }


    
    @PostMapping("/register")
    public UserResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) 
    {
        return userService.register(request);
    }


    
    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) 
    {
        return userService.login(request);
    }


    @PostMapping("/forgot-password/request")
    public void requestPasswordReset(@Valid @RequestBody ForgotPasswordRequestDTO request)
    {
        userService.requestPasswordReset(request.getEmail());
    }



    @PostMapping("/forgot-password/verify")
    public void verifyPasswordReset(@Valid @RequestBody VerifyResetCodeDTO request)
    {
        userService.verifyPasswordResetCode(request.getEmail(),request.getCode());
    }



    @PostMapping("/forgot-password/reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordDTO request)
    {
        userService.resetForgottenPassword(request.getEmail(), request.getCode(), request.getNewPassword());
    }
    
}