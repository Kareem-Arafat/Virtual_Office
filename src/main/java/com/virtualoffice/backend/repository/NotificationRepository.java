package com.virtualoffice.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualoffice.backend.entity.Notification;

public interface NotificationRepository
    extends JpaRepository<Notification, Long>
{
    // هاتلي كل النونيفيكشن بتاعة اليوزر ده، ورتبهم من الأحدث للأقدم.
    List<Notification> findByRecipientIdOrderByTimestampDesc(Long recipientId);

    // عد كام نوتيفيكشن لليوزر ده لسا مقراهاش
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    void deleteByRecipientId(Long recipientId);
}
