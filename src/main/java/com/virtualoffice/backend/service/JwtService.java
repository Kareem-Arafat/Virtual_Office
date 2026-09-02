package com.virtualoffice.backend.service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService
{
    private String secretKey;



    public JwtService(@Value("${jwt.secret}") String secretKey)
    {
        if(secretKey == null || secretKey.length() < 32)
        {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 characters");
        }

        this.secretKey = secretKey;
    }



    private Key getSigningKey()
    {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }




    public String generateToken(String username)
    {
        // بيستخدم Jwts.builder() عشان يبني التوكن
        // .subject(username) عشان يحط اسم المستخدم في التوكن
        // .issuedAt(new Date()) عشان يحط وقت الاصدار
        // .expiration(new Date(System.currentTimeMillis() + 86400000)) عشان يحط وقت الانتهاء (هنا 24 ساعة)
        //  System.currentTimeMillis() بتجيب الوقت بالثانيه
        //  و بنضيف عليه 86400000 ملي ثانيه (24 ساعة) عشان يحدد وقت الانتهاء
        // .signWith(getSigningKey()) عشان يوقع التوكن بالمفتاح
        // .compact() عشان يرجع التوكن كسترينج
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(getSigningKey())
            .compact();
    }



    // فانكشن بتستخرج اسم المستخدم من التوكن عشان نشوف التوكن صالح ولا لا
    public String extractUsername(String token)
    {
        try
        {
            return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        }
        catch(JwtException | IllegalArgumentException exception)
        {
            return null;
        }
    }


    

    public boolean isTokenValid(String token, String username)
    {
        String tokenUsername = extractUsername(token);
        return tokenUsername != null && tokenUsername.equals(username);
    }
}
