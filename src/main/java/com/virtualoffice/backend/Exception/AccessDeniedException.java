package com.virtualoffice.backend.Exception;

public class AccessDeniedException extends RuntimeException
{
    public AccessDeniedException(String message)
    {
        super(message);
    }
}