package com.virtualoffice.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualoffice.backend.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>
{
    // هات كل الرسائل بتاعة الروم دي ورتبها من الأقدم للأحدث
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(Long roomId);

    void deleteByRoomId(Long roomId);
    void deleteBySenderId(Long senderId);
}
