package com.virtualoffice.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.dto.RoomRequestDTO;
import com.virtualoffice.backend.dto.RoomResponseDTO;
import com.virtualoffice.backend.dto.AdminUserDTO;
import com.virtualoffice.backend.entity.Room;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.RoomRepository;
import com.virtualoffice.backend.repository.UserRepository;
import com.virtualoffice.backend.repository.ChatMessageRepository;

@Service
public class RoomService
{
    private RoomRepository roomRepository;
    private UserRepository userRepository;
    private EmailService emailService;
    private NotificationService notificationService;
    private ChatMessageRepository chatMessageRepository;



    public RoomService(RoomRepository roomRepository, UserRepository userRepository, EmailService emailService, NotificationService notificationService, ChatMessageRepository chatMessageRepository)
    {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.chatMessageRepository = chatMessageRepository;
    }




    public RoomResponseDTO createRoom(RoomRequestDTO request, String username)
    {
        User creator = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(request.getName() == null || request.getName().trim().isEmpty())
        {
            throw new IllegalArgumentException("Room name is required");
        }

        if (creator.getRole() != User.UserRole.MANAGER && creator.getRole() != User.UserRole.TEAM_LEADER)
        {
            throw new AccessDeniedException("Only managers and team leaders can create room");
        }

        Room room = new Room();

        room.setName(request.getName().trim());
        room.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        room.setCreatedBy(creator);

        room.getMembers().add(creator); // ضيف اللي عمل الروم جوا الروم

        roomRepository.save(room);

        return new RoomResponseDTO
        (
            room.getId(),
            room.getName(),
            room.getDescription(),
            room.getCreatedBy().getUsername(),
            room.getCreatedAt(),
            room.getMembers().size(),
            true,
            true
        );
    }




    private RoomResponseDTO addMemberToRoom(Long roomId, Long userId, String currentUsername)
    {
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(() -> new UserNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        User targetUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(room.getCreatedBy() == null || !room.getCreatedBy().getId().equals(currentUser.getId()))
        {
            throw new AccessDeniedException("Only the room creator can add members");
        }

        Long ID = currentUser.getId();
        User teamLeader = targetUser.getTeamLeader();
        boolean memberAdded;

        if (currentUser.getRole() == User.UserRole.MANAGER)
        {
            memberAdded = room.getMembers().add(targetUser);
        }
        else if (currentUser.getRole() == User.UserRole.TEAM_LEADER)
        {
            if (targetUser.getTeamLeader() == null || !teamLeader.getId().equals(ID))
            {
                throw new AccessDeniedException("You can only add your own team members");
            }

            memberAdded = room.getMembers().add(targetUser);
        }
        else
        {
            throw new AccessDeniedException("You are not allowed to add members");
        }

        roomRepository.save(room);

        if(memberAdded && !targetUser.getId().equals(currentUser.getId()))
        {
            notificationService.sendNotification(targetUser, "You were added to room: " + room.getName());
            emailService.sendRoomAddedEmail(targetUser.getEmail(), targetUser.getUsername(), room.getName());
        }

        return new RoomResponseDTO
        (
            room.getId(),
            room.getName(),
            room.getDescription(),
            room.getCreatedBy().getUsername(),
            room.getCreatedAt(),
            room.getMembers().size(),
            true,
            true
        );
    }




    public List<RoomResponseDTO> getRoomsForUser(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Room> rooms = roomRepository.findByMembersId(user.getId());

        List<RoomResponseDTO> response = new ArrayList<>();

        for (Room room : rooms)
        {
            RoomResponseDTO dto = new RoomResponseDTO
            (
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getCreatedBy() == null ? "Former user" : room.getCreatedBy().getUsername(),
                room.getCreatedAt(),
                room.getMembers().size(),
                true,
                room.getCreatedBy() != null && room.getCreatedBy().getId().equals(user.getId())
            );

            response.add(dto);
        }

        return response;
    }




    public List<RoomResponseDTO> getAllRoomsForManager(String username)
    {
        User manager = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (manager.getRole() != User.UserRole.MANAGER)
        {
            throw new AccessDeniedException("Only managers can view all rooms");
        }

        List<Room> rooms = roomRepository.findAll();

        List<RoomResponseDTO> response = new ArrayList<>();

        for (Room room : rooms)
        {
            RoomResponseDTO dto = new RoomResponseDTO
            (
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getCreatedBy() == null ? "Former user" : room.getCreatedBy().getUsername(),
                room.getCreatedAt(),
                room.getMembers().size(),
                room.getMembers().contains(manager),
                room.getCreatedBy() != null && room.getCreatedBy().getId().equals(manager.getId())
            );
            
            response.add(dto);
        }

        return response;
    }





    public RoomResponseDTO addMemberToRoomByStaffId(Long roomId, String staffId, String currentUsername)
    {
        if(staffId == null || staffId.trim().isEmpty())
        {
            throw new IllegalArgumentException("Staff ID is required");
        }

        User targetUser = userRepository.findByStaffId(staffId.trim().toUpperCase()).orElseThrow(() -> new UserNotFoundException("No employee has this Staff ID"));

        return addMemberToRoom(roomId, targetUser.getId(), currentUsername);
    }




    public List<AdminUserDTO> getRoomMembers(Long roomId, String username)
    {
        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        if(!room.getMembers().contains(currentUser))
        {
            throw new AccessDeniedException("You are not a member of this room");
        }

        List<AdminUserDTO> response = new ArrayList<>();

        for(User member : room.getMembers())
        {
            response.add(new AdminUserDTO(member.getId(), member.getUsername(), member.getStaffId(), member.getEmail(), member.getRole(), member.getTeamLeader() == null ? null : member.getTeamLeader().getUsername()));
        }

        return response;
    }




    @Transactional
    public void deleteRoom(Long roomId, String username)
    {
        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        boolean isCreator = room.getCreatedBy() != null && room.getCreatedBy().getId().equals(currentUser.getId());

        if(currentUser.getRole() != User.UserRole.MANAGER && !isCreator)
        {
            throw new AccessDeniedException("Only the manager or room creator can delete this room");
        }

        chatMessageRepository.deleteByRoomId(roomId);
        roomRepository.delete(room);
    }





    public RoomResponseDTO removeMember(Long roomId, Long userId, String username)
    {
        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        User targetUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(room.getCreatedBy() == null || !room.getCreatedBy().getId().equals(currentUser.getId()))
        {
            throw new AccessDeniedException("Only the room creator can remove members");
        }

        if(targetUser.getId().equals(currentUser.getId()))
        {
            throw new AccessDeniedException("The room creator cannot remove themselves");
        }

        room.getMembers().remove(targetUser);
        roomRepository.save(room);

        return new RoomResponseDTO(room.getId(), room.getName(), room.getDescription(), currentUser.getUsername(), room.getCreatedAt(), room.getMembers().size(), true, true);
    }


    

    public void leaveRoom(Long roomId, String username)
    {
        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        if(!room.getMembers().contains(currentUser))
        {
            throw new AccessDeniedException("You are not a member of this room");
        }

        if(room.getCreatedBy() != null && room.getCreatedBy().getId().equals(currentUser.getId()))
        {
            throw new AccessDeniedException("The room creator must delete the room instead of leaving");
        }

        room.getMembers().remove(currentUser);
        roomRepository.save(room);
    }
}
