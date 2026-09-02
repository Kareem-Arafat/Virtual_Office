package com.virtualoffice.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.virtualoffice.backend.dto.RoomRequestDTO;
import com.virtualoffice.backend.dto.RoomResponseDTO;
import com.virtualoffice.backend.dto.AdminUserDTO;
import com.virtualoffice.backend.service.RoomService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
public class RoomController
{
    private RoomService roomService;



    public RoomController(RoomService roomService)
    {
        this.roomService = roomService;
    }



    @PostMapping
    public RoomResponseDTO createRoom(@Valid @RequestBody RoomRequestDTO request,Authentication authentication)
    {
        String username = authentication.getName();

        return roomService.createRoom(request, username);
    }




    @GetMapping
    public List<RoomResponseDTO> getMyRooms(Authentication authentication)
    {
        String username = authentication.getName();

        return roomService.getRoomsForUser(username);
    }





    @GetMapping("/all")
    public List<RoomResponseDTO> getAllRooms(Authentication authentication)
    {
        String username = authentication.getName();

        return roomService.getAllRoomsForManager(username);
    }


    
    @PostMapping("/{roomId}/members")
    public RoomResponseDTO addMemberByStaffId(@PathVariable Long roomId, @RequestParam String staffId, Authentication authentication)
    {
        return roomService.addMemberToRoomByStaffId(roomId, staffId, authentication.getName());
    }




    @GetMapping("/{roomId}/members")
    public List<AdminUserDTO> getRoomMembers(@PathVariable Long roomId, Authentication authentication)
    {
        return roomService.getRoomMembers(roomId, authentication.getName());
    }



    @DeleteMapping("/{roomId}")
    public void deleteRoom(@PathVariable Long roomId, Authentication authentication)
    {
        roomService.deleteRoom(roomId, authentication.getName());
    }



    @DeleteMapping("/{roomId}/members/{userId}")
    public RoomResponseDTO removeMember(@PathVariable Long roomId, @PathVariable Long userId, Authentication authentication)
    {
        return roomService.removeMember(roomId, userId, authentication.getName());
    }



    @DeleteMapping("/{roomId}/leave")
    public void leaveRoom(@PathVariable Long roomId, Authentication authentication)
    {
        roomService.leaveRoom(roomId, authentication.getName());
    }
}
