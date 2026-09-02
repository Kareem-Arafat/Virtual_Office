package com.virtualoffice.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.virtualoffice.backend.entity.Post;
import com.virtualoffice.backend.entity.Room;
import com.virtualoffice.backend.entity.Task;
import com.virtualoffice.backend.entity.User;
import com.virtualoffice.backend.repository.PostRepository;
import com.virtualoffice.backend.repository.RoomRepository;
import com.virtualoffice.backend.repository.TaskRepository;
import com.virtualoffice.backend.repository.UserRepository;

@Service
public class AiService
{
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final PostRepository postRepository;


    @Value("${groq.api.key}")
    private String apiKey;


    private String API_URL =
        "https://api.groq.com/openai/v1/chat/completions";


    public AiService(UserRepository userRepository, TaskRepository taskRepository, RoomRepository roomRepository, PostRepository postRepository)
    {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.roomRepository = roomRepository;
        this.postRepository = postRepository;
    }



    

    public String getAiResponse(String prompt, String username)
    {

        if(username == null)
        {
            String systemPrompt =
                """
                You are RafiQ AI, the assistant for the RafiQ Virtual Office website.

                The current visitor is not logged in.

                Your job is to:
                - Explain what RafiQ is
                - Help the visitor understand the website
                - Explain how to sign up
                - Explain how to login
                - Explain the available website features
                - Help the visitor know what they can do after creating an account

                Rules:

                1. Do not claim access to private company data.
                2. Do not invent user information.
                3. Do not answer unrelated general questions.
                4. Keep answers short, clear and helpful.
                """;

            return callGroq(systemPrompt, prompt);
        }


        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        List<Task> tasks = getTasksForAi(user);
        List<Room> rooms = roomRepository.findByMembersId(user.getId());
        List<Post> posts = postRepository.findByOrderByCreatedAtDesc();

        String officeContext = buildOfficeContext(user, tasks, rooms, posts);


        String systemPrompt =
            """
            You are RafiQ AI, the assistant inside the RafiQ Virtual Office website.

            Your job is to help users use the RafiQ website and understand
            their available office information.

            You can help with:
            - Tasks and task status
            - Rooms and team collaboration
            - Office posts
            - User roles
            - Productivity
            - Explaining how to use features of the Virtual Office website

            Rules:

            1. Never invent private company data.
            2. Never reveal information the current user should not access.
            3. Use the provided Virtual Office data when answering questions
            about the current user's tasks, rooms, posts or team.
            4. You may explain how Virtual Office features generally work.
            5. If requested office data is not available, say you do not have
            enough information.
            6. Do not answer unrelated questions that have nothing to do with
            work or the RafiQ Virtual Office.
            7. Keep answers short, clear and helpful.

            Virtual Office Data:

            """
            + officeContext;


        return callGroq(systemPrompt, prompt);
    }




    private List<Task> getTasksForAi(User user)
    {
        switch(user.getRole())
        {
            case MANAGER:
                return taskRepository.findAll();

            case TEAM_LEADER:
                return taskRepository.findByUserTeamLeaderId(user.getId());

            case DEVELOPER:
                return taskRepository.findByUserId(user.getId());

            default:
                return new ArrayList<>();
        }
    }



    

    private String buildOfficeContext(User user, List<Task> tasks, List<Room> rooms, List<Post> posts)
    {
        StringBuilder context = new StringBuilder();


        context.append("CURRENT USER\n");

        context.append("Username: ").append(user.getUsername()).append("\n");

        context.append("Email: ").append(user.getEmail()).append("\n");

        context.append("Role: ").append(user.getRole()).append("\n");


        if (user.getTeamLeader() != null)
        {
            context.append("Team Leader: ").append(user.getTeamLeader().getUsername()).append("\n");
        }


        context.append("\nTASKS\n");

        long completedCount = tasks.stream().filter(task -> task.getStatus() == Task.TaskStatus.DONE).count();
        long pendingCount = tasks.size() - completedCount;
        long overdueCount = tasks.stream().filter(task -> task.getStatus() != Task.TaskStatus.DONE && task.getDeadline() != null && task.getDeadline().isBefore(LocalDate.now())).count();

        context.append("Total visible tasks: ").append(tasks.size()).append("\n");
        context.append("Completed tasks: ").append(completedCount).append("\n");
        context.append("Pending or in-progress tasks: ").append(pendingCount).append("\n");
        context.append("Overdue tasks: ").append(overdueCount).append("\n");


        if (tasks.isEmpty())
        {
            context.append("No tasks assigned.\n");
        }
        else
        {
            for (Task task : tasks)
            {
                context.append("- ").append(task.getTitle()).append(" | Status: ").append(task.getStatus());

                if (task.getDescription() != null)
                {
                    context.append(" | Description: ").append(task.getDescription());
                }

                if(task.getDeadline() != null)
                {
                    context.append(" | Deadline: ").append(task.getDeadline());

                    if(task.getStatus() != Task.TaskStatus.DONE && task.getDeadline().isBefore(LocalDate.now()))
                    {
                        context.append(" | OVERDUE");
                    }
                }

                if(task.getUser() != null)
                {
                    context.append(" | Assigned to: ").append(task.getUser().getUsername());
                }

                context.append("\n");
            }
        }


        context.append("\nROOMS\n");


        if (rooms.isEmpty())
        {
            context.append("User is not a member of any rooms.\n");
        }
        else
        {
            for (Room room : rooms)
            {
                context.append("- ").append(room.getName());

                if (room.getDescription() != null)
                {
                    context.append(" | ").append(room.getDescription());
                }

                context.append("\n");
            }
        }


        context.append("\nLATEST OFFICE POSTS\n");


        int postCount = 0;

        for (Post post : posts)
        {
            if (postCount >= 10)
            {
                break;
            }

            String username = "Former user";

            if (post.getAuthor() != null)
            {
                username = post.getAuthor().getUsername();
            }

            context.append("- ").append(username).append(": ").append(post.getContent()).append("\n");

            postCount++;
        }


        if (postCount == 0)
        {
            context.append("No office posts available.\n");
        }


        return context.toString();
    }





    private String callGroq(String systemPrompt, String userPrompt)
    {
        RestTemplate restTemplate = new RestTemplate();


        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);


        Map<String, Object> systemMessage = new HashMap<>();

        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);


        Map<String, Object> userMessage = new HashMap<>();

        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);


        List<Map<String, Object>> messages = new ArrayList<>();

        messages.add(systemMessage);
        messages.add(userMessage);


        Map<String, Object> body = new HashMap<>();

        body.put("model","openai/gpt-oss-20b");

        body.put("messages", messages);


        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);


        ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, request, Map.class);

        Map responseBody = response.getBody();


        List<Map<String, Object>> choices = (List<Map<String, Object>>)responseBody.get("choices");


        Map<String, Object> firstChoice = choices.get(0);


        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");


        return (String) message.get("content");
    }
}
