package com.virtualoffice.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualoffice.backend.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>
{
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId); // هات كل الكمنتات اللي ف البوست ده بالترتيب

    void deleteByUserId(Long userId);
}
