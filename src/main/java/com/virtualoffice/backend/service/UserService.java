package com.virtualoffice.backend.service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.virtualoffice.backend.Exception.AccessDeniedException;
import com.virtualoffice.backend.Exception.InvalidCredentialsException;
import com.virtualoffice.backend.Exception.UserAlreadyExistsException;
import com.virtualoffice.backend.Exception.UserNotFoundException;
import com.virtualoffice.backend.dto.AdminUserDTO;
import com.virtualoffice.backend.dto.LoginRequestDTO;
import com.virtualoffice.backend.dto.LoginResponseDTO;
import com.virtualoffice.backend.dto.PasswordChangeRequestDTO;
import com.virtualoffice.backend.dto.ProfileUpdateDTO;
import com.virtualoffice.backend.dto.RegisterRequestDTO;
import com.virtualoffice.backend.dto.UserResponseDTO;
import com.virtualoffice.backend.entity.Post;
import com.virtualoffice.backend.entity.Room;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.ChatMessageRepository;
import com.virtualoffice.backend.repository.CommentRepository;
import com.virtualoffice.backend.repository.NotificationRepository;
import com.virtualoffice.backend.repository.PostRepository;
import com.virtualoffice.backend.repository.RoomRepository;
import com.virtualoffice.backend.repository.TaskRepository;
import com.virtualoffice.backend.repository.UserRepository;


@Service
public class UserService 
{
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private EmailService emailService;
    private TaskRepository taskRepository;
    private RoomRepository roomRepository;
    private PostRepository postRepository;
    private ChatMessageRepository chatMessageRepository;
    private CommentRepository commentRepository;
    private NotificationRepository notificationRepository;
    private NotificationService notificationService;
    private final SecureRandom secureRandom = new SecureRandom();


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService, TaskRepository taskRepository, RoomRepository roomRepository, PostRepository postRepository, ChatMessageRepository chatMessageRepository, CommentRepository commentRepository, NotificationRepository notificationRepository, NotificationService notificationService) 
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.taskRepository = taskRepository;
        this.roomRepository = roomRepository;
        this.postRepository = postRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.commentRepository = commentRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }




    public UserResponseDTO getUserByUsername(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserResponseDTO
        (
            user.getId(),
            user.getUsername(),
            user.getStaffId(),
            user.getEmail(),
            user.getRole(),
            user.getBio(),
            user.getPhone()
        );
    }




    public List<AdminUserDTO> getVisibleUsers(String username)
    {
        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        List<User> users;

        if(currentUser.getRole() == User.UserRole.MANAGER)
        {
            users = userRepository.findAll();
        }
        else if(currentUser.getRole() == User.UserRole.TEAM_LEADER)
        {
            users = new ArrayList<>(userRepository.findByTeamLeaderId(currentUser.getId()));
            users.add(0, currentUser);
        }
        else
        {
            users = List.of(currentUser);
        }

        List<AdminUserDTO> response = new ArrayList<>();

        for(User user : users)
        {
            response.add(new AdminUserDTO
            (
                user.getId(),
                user.getUsername(),
                user.getStaffId(),
                user.getEmail(),
                user.getRole(),
                user.getTeamLeader() == null ? null : user.getTeamLeader().getUsername()
            ));
        }

        return response;
    }




    @Transactional
    public synchronized UserResponseDTO register(RegisterRequestDTO request)
    {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        if(userRepository.existsByUsername(username))
        {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(email))
        {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash
        (
            passwordEncoder.encode(request.getPassword())
        );


        User.UserRole role;
        if(userRepository.count() == 0)
        {
            role = User.UserRole.MANAGER;
        }
        else
        {
            role = User.UserRole.DEVELOPER;
        }

        user.setRole(role);
        user.setStaffId(generateUniqueStaffId(role));

        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername(), user.getStaffId());

        return new UserResponseDTO
        (
            user.getId(),
            user.getUsername(),
            user.getStaffId(),
            user.getEmail(),
            user.getRole(),
            user.getBio(),
            user.getPhone()
        );
    }


    /*  
     كان اسمها 
     userResponseDTO 
     بس كانت مبترجعش توكن بترجع بيانات يوزر ف غيرنا اسمها ل  فانكشن جديده 
     عشان ترجع بيانات اليوزر و معاها توكن
    */
    public LoginResponseDTO login(LoginRequestDTO request)
    {
        User user = userRepository.findByUsername(request.getUsername().trim()).orElseThrow(() -> new InvalidCredentialsException("Invalid username, Staff ID or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
        {
            throw new InvalidCredentialsException("Invalid username, Staff ID or password");
        }


        if(user.getStaffId() == null || !user.getStaffId().equals(request.getStaffId().trim().toUpperCase()))
        {
            throw new InvalidCredentialsException("Invalid username, Staff ID or password");
        }


        String token = jwtService.generateToken(user.getUsername());

        UserResponseDTO userResponse = new UserResponseDTO
        (
            user.getId(),
            user.getUsername(),
            user.getStaffId(),
            user.getEmail(),
            user.getRole(),
            user.getBio(),
            user.getPhone()
        );

        return new LoginResponseDTO(token, userResponse);
    }





    public UserResponseDTO updateProfile(String currentUsername, ProfileUpdateDTO request)
    {
        User user = userRepository.findByUsername(currentUsername).orElseThrow(() -> new UserNotFoundException("User not found"));

        String newUsername = request.getUsername().trim();
        String newEmail = request.getEmail().trim();

        if(!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername))
        {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if(!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail))
        {
            throw new UserAlreadyExistsException("Email already exists");
        }

        user.setUsername(newUsername);
        user.setEmail(newEmail);

        if(request.getBio() == null)
        {
            user.setBio(null);
        }
        else
        {
            user.setBio(request.getBio().trim());
        }


        if(request.getPhone() == null)
        {
            user.setPhone(null);
        }
        else
        {
            user.setPhone(request.getPhone().trim());
        }

        userRepository.save(user);

        return new UserResponseDTO
        (
            user.getId(),
            user.getUsername(),
            user.getStaffId(),
            user.getEmail(),
            user.getRole(),
            user.getBio(),
            user.getPhone()
        );
    }




    public void changePassword(String username, PasswordChangeRequestDTO request)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash()))
        {
            throw new InvalidCredentialsException("Invalid old password");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);

        userRepository.save(user);

        emailService.sendPasswordChangeAlert(user.getEmail(), user.getUsername());
    }




    @Transactional
    public void deleteAccount(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        deleteUserData(user);
    }




    @Transactional
    public void deleteUserByManager(Long userId, String managerUsername)
    {
        User manager = userRepository.findByUsername(managerUsername).orElseThrow(() ->new UserNotFoundException("Manager not found"));

        if(manager.getRole() != User.UserRole.MANAGER)
        {
            throw new AccessDeniedException("Only managers can remove employees");
        }

        User user = userRepository.findById(userId).orElseThrow(() ->new UserNotFoundException("User not found"));

        if(user.getId().equals(manager.getId()))
        {
            throw new AccessDeniedException("You cannot remove your own account here");
        }

        deleteUserData(user);
    }




    private void deleteUserData(User user)
    {
        taskRepository.deleteByUserId(user.getId());

        chatMessageRepository.deleteBySenderId(user.getId());

        commentRepository.deleteByUserId(user.getId());

        notificationRepository.deleteByRecipientId(user.getId());


        List<User> employees = userRepository.findByTeamLeaderId(user.getId());

        for(User employee : employees)
        {
            employee.setTeamLeader(null);
            userRepository.save(employee);
        }


        for(Room room : user.getRooms())
        {
            room.getMembers().remove(user);
            roomRepository.save(room);
        }


        List<Post> posts = postRepository.findByAuthorId(user.getId());

        for(Post post : posts)
        {
            post.setAuthor(null);
            postRepository.save(post);
        }


        for(Post likedPost : new ArrayList<>(user.getLikedPosts()))
        {
            likedPost.getLikedByUsers().remove(user);
            postRepository.save(likedPost);
        }


        List<Room> createdRooms = roomRepository.findByCreatedById(user.getId());

        for(Room room : createdRooms)
        {
            room.setCreatedBy(null);
            roomRepository.save(room);
        }


        userRepository.delete(user);
    }



    
    public void updateUserRole(Long userId, User.UserRole newRole, String managerUsername)
    {
        User manager = userRepository.findByUsername(managerUsername).orElseThrow(() -> new UserNotFoundException("Manager not found"));

        if (manager.getRole() != User.UserRole.MANAGER)
        {
            throw new AccessDeniedException("Only managers can update user role");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getId().equals(manager.getId()))
        {
            throw new AccessDeniedException("You cannot change your own manager role");
        }

        if(user.getRole() == newRole)
        {
            return;
        }

        if(user.getRole() == User.UserRole.TEAM_LEADER && newRole != User.UserRole.TEAM_LEADER)
        {
            List<User> teamMembers = userRepository.findByTeamLeaderId(user.getId());

            for(User teamMember : teamMembers)
            {
                teamMember.setTeamLeader(null);
                userRepository.save(teamMember);
            }
        }

        if(newRole != User.UserRole.DEVELOPER)
        {
            user.setTeamLeader(null);
        }

        String newStaffId = generateUniqueStaffId(newRole);
        user.setRole(newRole);
        user.setStaffId(newStaffId);

        userRepository.save(user);

        notificationService.sendNotification(user, "Your role was changed to " + newRole.name() + ". New Staff ID: " + newStaffId);
        emailService.sendRoleChangedEmail(user.getEmail(), user.getUsername(), newRole.name(), newStaffId);
    }




    public void addEmployeeToMyTeam(String teamLeaderUsername, String staffId)
    {
        User teamLeader = requireTeamLeader(teamLeaderUsername);
        User employee = findDeveloperByStaffId(staffId);

        if(employee.getTeamLeader() != null)
        {
            throw new IllegalArgumentException("This developer already belongs to a team");
        }

        employee.setTeamLeader(teamLeader);
        userRepository.save(employee);
        notificationService.sendNotification(employee, "You were added to " + teamLeader.getUsername() + "'s team");
    }




    public void removeEmployeeFromMyTeam(String teamLeaderUsername, String staffId)
    {
        User teamLeader = requireTeamLeader(teamLeaderUsername);
        User employee = findDeveloperByStaffId(staffId);

        if(employee.getTeamLeader() == null || !employee.getTeamLeader().getId().equals(teamLeader.getId()))
        {
            throw new AccessDeniedException("You can only remove your own team member");
        }

        employee.setTeamLeader(null);
        userRepository.save(employee);
        notificationService.sendNotification(employee, "You were removed from " + teamLeader.getUsername() + "'s team");
    }





    private User requireTeamLeader(String username)
    {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        if(user.getRole() != User.UserRole.TEAM_LEADER)
        {
            throw new AccessDeniedException("Only Team Leaders can manage a team");
        }
        return user;
    }




    private User findDeveloperByStaffId(String staffId)
    {
        if(staffId == null || staffId.trim().isEmpty())
        {
            throw new IllegalArgumentException("Staff ID is required");
        }

        User employee = userRepository.findByStaffId(staffId.trim().toUpperCase()).orElseThrow(() -> new UserNotFoundException("No employee has this Staff ID"));
        if(employee.getRole() != User.UserRole.DEVELOPER)
        {
            throw new IllegalArgumentException("Only Developers can be assigned to a team");
        }
        return employee;
    }





    private String generateUniqueStaffId(User.UserRole role)
    {
        String staffId, rolePrefix;

        switch(role)
        {
            case TEAM_LEADER:
                rolePrefix = "TL";
                break;

            case DEVELOPER:
                rolePrefix = "DEV";
                break;

            case MANAGER:
                rolePrefix = "MAN";
                break;

            default:
                throw new IllegalArgumentException("Invalid user role");
        }

        do
        {
            int randomNumber = 10000 + secureRandom.nextInt(90000);

            staffId = rolePrefix + "-" + randomNumber;
        }
        while(userRepository.existsByStaffId(staffId));

        return staffId;
    }






    public void requestPasswordReset(String email)
    {
        User user = userRepository.findByEmail(email).orElse(null);

        if(user == null)
        {
            return;
        }

        int code = 100000 + secureRandom.nextInt(900000);


        user.setTempCode(String.valueOf(code));

        user.setTempCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        emailService.sendPasswordResetCode(user.getEmail(),String.valueOf(code));
    }




    public void verifyPasswordResetCode(String email, String code)
    {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("Invalid email or reset code"));


        if(user.getTempCode() == null ||!user.getTempCode().equals(code))
        {
            throw new InvalidCredentialsException("Invalid reset code");
        }


        if(user.getTempCodeExpiresAt() == null || LocalDateTime.now().isAfter(user.getTempCodeExpiresAt()))
        {
            throw new InvalidCredentialsException("Reset code expired");
        }
    }





    public void resetForgottenPassword(String email, String code, String newPassword)
    {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("Invalid email or reset code"));
        

        verifyPasswordResetCode(email, code);


        user.setPasswordHash(passwordEncoder.encode(newPassword));

        user.setTempCode(null);

        user.setTempCodeExpiresAt(null);

        userRepository.save(user);

        emailService.sendPasswordChangeAlert(user.getEmail(),user.getUsername());
    }
}
