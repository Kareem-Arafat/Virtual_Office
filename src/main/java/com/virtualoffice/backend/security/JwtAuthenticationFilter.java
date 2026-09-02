package com.virtualoffice.backend.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.virtualoffice.backend.repository.UserRepository;
import com.virtualoffice.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository)
    {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }


    /*  HttpServletRequest request
      بيحتوي على كل المعلومات عن الريكويست اللي جايه من العميل زي
     GET /projects
     Authorization: Bearer eyJhbGc...

     HttpServletResponse response
      بيحتوي على كل المعلومات عن الرد اللي هيعود للعميل

     FilterChain filterChain
     بيحتوي على كل الفلاتر اللي موجوده في الابلكيشن اللي هو لما تخلص الفلتر اللي انت عامله بيول للسبرينج يروح للفلاتر التانيه
    */
   
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer "))
        {
            filterChain.doFilter(request, response);
            return;
        }


        String token = authHeader.substring(7);

        String username = jwtService.extractUsername(token);


        if(username != null && jwtService.isTokenValid(token, username))
        {
            boolean userExists = userRepository.existsByUsername(username);

            if(!userExists)
            {
                SecurityContextHolder.clearContext();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                return;
            }


            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,null,null);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }


        filterChain.doFilter(request, response);
    }
}