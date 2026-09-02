package com.virtualoffice.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO
{
    @NotBlank
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank
    private String code;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
    private String newPassword;


    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }
}
