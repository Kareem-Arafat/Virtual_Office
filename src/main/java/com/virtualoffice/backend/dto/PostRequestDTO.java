package com.virtualoffice.backend.dto;

import jakarta.validation.constraints.Size;

public class PostRequestDTO
{
    @Size(max = 255, message = "Post cannot exceed 255 characters")
    private String content;
    private String imageUrl;


    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }


    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
}
