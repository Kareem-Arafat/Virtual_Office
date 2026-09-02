package com.virtualoffice.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequestDTO
{
    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 255, message = "Comment cannot exceed 255 characters")
    private String text;


    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
