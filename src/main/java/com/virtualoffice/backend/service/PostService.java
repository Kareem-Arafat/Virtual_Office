package com.virtualoffice.backend.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;

import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.dto.CommentRequestDTO;
import com.virtualoffice.backend.dto.CommentResponseDTO;
import com.virtualoffice.backend.dto.PostRequestDTO;
import com.virtualoffice.backend.dto.PostResponseDTO;
import com.virtualoffice.backend.entity.Comment;
import com.virtualoffice.backend.entity.Post;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.CommentRepository;
import com.virtualoffice.backend.repository.PostRepository;
import com.virtualoffice.backend.repository.UserRepository;

@Service
public class PostService
{
    private static final int MAX_ENCODED_MEDIA_LENGTH = 21_000_000;

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;


    public PostService(PostRepository postRepository, CommentRepository commentRepository, UserRepository userRepository)
    {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }





    public PostResponseDTO createPost(PostRequestDTO request, String username)
    {
        User user = getUser(username);
        String content = request.getContent() == null ? "" : request.getContent().trim();
        String imageUrl = validatePostMedia(request.getImageUrl());

        if(content.isEmpty() && imageUrl == null)
        {
            throw new IllegalArgumentException("Post content or media is required");
        }

        Post post = new Post();

        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setAuthor(user);

        postRepository.save(post);


        List<CommentResponseDTO> comments = new ArrayList<>();


        return new PostResponseDTO
        (
            post.getId(),
            post.getContent(),
            post.getImageUrl(),
            user.getUsername(),
            post.getCreatedAt(),
            0,
            false,
            true,
            comments
        );
    }





    public List<PostResponseDTO> getAllPosts(String username)
    {
        User currentUser = getUser(username);

        List<Post> posts = postRepository.findByOrderByCreatedAtDesc();
        List<PostResponseDTO> result = new ArrayList<>();


        for(Post post : posts)
        {
            boolean likedByMe = post.getLikedByUsers().contains(currentUser);
            boolean postOwner = false;

            if(post.getAuthor() != null)
            {
                postOwner = post.getAuthor().getId().equals(currentUser.getId());
            }


            boolean isManager = currentUser.getRole() == User.UserRole.MANAGER;

            boolean canDeletePost = postOwner || isManager;


            String authorUsername = "Deleted User";

            if(post.getAuthor() != null)
            {
                authorUsername = post.getAuthor().getUsername();
            }


            List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId());

            List<CommentResponseDTO> commentDTOs = new ArrayList<>();


            for(Comment comment : comments)
            {
                boolean commentOwner = comment.getUser().getId().equals(currentUser.getId());

                boolean canDeleteComment = commentOwner || postOwner;


                CommentResponseDTO commentDTO = new CommentResponseDTO
                (
                    comment.getId(),
                    comment.getText(),
                    comment.getUser().getUsername(),
                    comment.getCreatedAt(),
                    canDeleteComment
                );

                commentDTOs.add(commentDTO);
            }


            PostResponseDTO postDTO = new PostResponseDTO
            (
                post.getId(),
                post.getContent(),
                post.getImageUrl(),
                authorUsername,

                post.getCreatedAt(),
                post.getLikedByUsers().size(),
                likedByMe,
                canDeletePost,
                commentDTOs
            );

            result.add(postDTO);
        }

        return result;
    }





    public PostResponseDTO likePost(Long postId, String username)
    {
        User currentUser = getUser(username);

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));


        if(post.getLikedByUsers().contains(currentUser))
        {
            post.getLikedByUsers().remove(currentUser);
        }
        else
        {
            post.getLikedByUsers().add(currentUser);
        }


        postRepository.save(post);


        boolean likedByMe = post.getLikedByUsers().contains(currentUser);
        boolean postOwner = post.getAuthor() != null && post.getAuthor().getId().equals(currentUser.getId());
        boolean isManager = currentUser.getRole() == User.UserRole.MANAGER;
        boolean canDeletePost = postOwner || isManager;


        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId());

        List<CommentResponseDTO> commentDTOs = new ArrayList<>();


        for(Comment comment : comments)
        {
            boolean commentOwner = comment.getUser().getId().equals(currentUser.getId());

            boolean canDeleteComment = commentOwner || postOwner;


            commentDTOs.add(new CommentResponseDTO
            (
                comment.getId(),
                comment.getText(),
                comment.getUser().getUsername(),
                comment.getCreatedAt(),
                canDeleteComment
            ));
        }


        return new PostResponseDTO
        (
            post.getId(),
            post.getContent(),
            post.getImageUrl(),
            post.getAuthor() == null ? "Deleted User" : post.getAuthor().getUsername(),
            post.getCreatedAt(),
            post.getLikedByUsers().size(),
            likedByMe,
            canDeletePost,
            commentDTOs
        );
    }





    public CommentResponseDTO addComment(Long postId, CommentRequestDTO request, String username)
    {
        User user = getUser(username);


        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));


        Comment comment = new Comment();

        comment.setText(request.getText().trim());
        comment.setPost(post);
        comment.setUser(user);

        commentRepository.save(comment);


        return new CommentResponseDTO
        (
            comment.getId(),
            comment.getText(),
            user.getUsername(),
            comment.getCreatedAt(),
            true
        );
    }



    

    public void deleteComment(Long postId, Long commentId, String username)
    {
        User user = getUser(username);

        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));


        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));


        if(!comment.getPost().getId().equals(postId))
        {
            throw new RuntimeException("Comment does not belong to this post");
        }


        boolean commentOwner = comment.getUser().getId().equals(user.getId());
        boolean postOwner = post.getAuthor() != null && post.getAuthor().getId().equals(user.getId());


        if(!commentOwner && !postOwner)
        {
            throw new AccessDeniedException("You cannot delete this comment");
        }

        commentRepository.delete(comment);
    }




    public void deletePost(Long postId, String username)
    {
        User user = getUser(username);


        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));


        boolean postOwner = post.getAuthor() != null && post.getAuthor().getId().equals(user.getId());
        boolean isManager = user.getRole() == User.UserRole.MANAGER;


        if(!postOwner && !isManager)
        {
            throw new AccessDeniedException("You cannot delete this post");
        }

        postRepository.delete(post);
    }




    private User getUser(String username)
    {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private String validatePostMedia(String mediaUrl)
    {
        if(mediaUrl == null || mediaUrl.isBlank())
        {
            return null;
        }

        String cleanUrl = mediaUrl.trim();

        if(cleanUrl.startsWith("https://") || cleanUrl.startsWith("http://"))
        {
            if(cleanUrl.length() > 2000)
            {
                throw new IllegalArgumentException("Media URL is too long");
            }
            return cleanUrl;
        }

        String[] allowedPrefixes =
        {
            "data:image/jpeg;base64,",
            "data:image/png;base64,",
            "data:image/gif;base64,",
            "data:image/webp;base64,",
            "data:video/mp4;base64,",
            "data:video/webm;base64,"
        };

        String matchedPrefix = null;
        for(String prefix : allowedPrefixes)
        {
            if(cleanUrl.startsWith(prefix))
            {
                matchedPrefix = prefix;
                break;
            }
        }

        if(matchedPrefix == null)
        {
            throw new IllegalArgumentException("Unsupported post media type");
        }

        if(cleanUrl.length() > MAX_ENCODED_MEDIA_LENGTH)
        {
            throw new IllegalArgumentException("Post media is too large");
        }

        try
        {
            Base64.getDecoder().decode(cleanUrl.substring(matchedPrefix.length()));
        }
        catch(IllegalArgumentException exception)
        {
            throw new IllegalArgumentException("Post media is invalid");
        }

        return cleanUrl;
    }
}
