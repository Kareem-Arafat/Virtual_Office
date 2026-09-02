package com.virtualoffice.backend.dto;

public class AiResponseDTO
{
    private String response;

    public AiResponseDTO(String response)
    {
        this.response = response;
    }

    public String getResponse()
    {
        return response;
    }
}