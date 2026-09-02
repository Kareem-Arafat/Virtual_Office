package com.virtualoffice.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualoffice.backend.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long>
{
    List<Post> findByOrderByCreatedAtDesc(); // هات كل البوستات و رتب من الاقدم للاحدث

    List<Post> findByAuthorId(Long authorId);
}