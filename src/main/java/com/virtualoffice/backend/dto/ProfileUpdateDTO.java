package com.virtualoffice.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateDTO
{
    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username cannot exceed 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email cannot exceed 254 characters")
    private String email;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    @Size(max = 30, message = "Phone cannot exceed 30 characters")
    private String phone;

    public ProfileUpdateDTO() {}

    public ProfileUpdateDTO(String username, String email, String bio, String phone)
    {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.phone = phone;
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

    public void setPhone(String phoneNumber)
    {
        this.phone = phoneNumber;
    }
}
