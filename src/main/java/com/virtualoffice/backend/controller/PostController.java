package com.virtualoffice.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.dto.CommentRequestDTO;
import com.virtualoffice.backend.dto.CommentResponseDTO;
import com.virtualoffice.backend.dto.PostRequestDTO;
import com.virtualoffice.backend.dto.PostResponseDTO;
import com.virtualoffice.backend.service.PostService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController
{
    private final PostService postService;


    public PostController(PostService postService)
    {
        this.postService = postService;
    }

    

    @PostMapping
    public PostResponseDTO createPost(@Valid @RequestBody PostRequestDTO request, Authentication authentication)
    {
        String username = authentication.getName();

        return postService.createPost(request, username);
    }



    @GetMapping
    public List<PostResponseDTO> getAllPosts(Authentication authentication)
    {
        String username = authentication.getName();

        return postService.getAllPosts(username);
    }



    @PostMapping("/{postId}/like")
    public PostResponseDTO likePost(@PathVariable Long postId, Authentication authentication)
    {
        String username = authentication.getName();

        return postService.likePost(postId, username);
    }



    @PostMapping("/{postId}/comments")
    public CommentResponseDTO addComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDTO request, Authentication authentication)
    {
        String username = authentication.getName();

        return postService.addComment(postId, request, username);
    }



    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId, Authentication authentication)
    {
        String username = authentication.getName();

        postService.deletePost(postId, username);
    }



    @DeleteMapping("/{postId}/comments/{commentId}")
    public void deleteComment(@PathVariable Long postId, @PathVariable Long commentId, Authentication authentication)
    {
        String username = authentication.getName();

        postService.deleteComment(postId, commentId, username);
    }
}
