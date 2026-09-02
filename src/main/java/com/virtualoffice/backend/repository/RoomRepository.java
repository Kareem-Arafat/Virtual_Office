package com.virtualoffice.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualoffice.backend.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long>
{
    List<Room> findByMembersId(Long userId);
    List<Room> findByCreatedById(Long userId);
}