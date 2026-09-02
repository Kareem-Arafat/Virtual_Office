package com.virtualoffice.backend.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virtualoffice.backend.entity.User;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> 
{
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByStaffId(String staffId);
    Optional<User> findByStaffId(String staffId);
    List<User> findByTeamLeaderId(Long teamLeaderId);
}
