package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.dto.TaskDto;
import com.Task_Management_System.TMS.dto.TeamDto;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.TeamMemberRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import com.Task_Management_System.TMS.security.CurrentUserProvider;
import com.Task_Management_System.TMS.security.JwtTokenProvider;
import com.Task_Management_System.TMS.security.SecurityConfig;
import com.Task_Management_System.TMS.security.UserDetailsServiceImpl;
import com.Task_Management_System.TMS.service.TaskService;
import com.Task_Management_System.TMS.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the API takes "who is making this request" from the JWT, not from IDs
 * that a client can put in the request body.
 */
@WebMvcTest({TeamController.class, TaskController.class})
@Import({SecurityConfig.class, CurrentUserProvider.class})
class RequestIdentityTest {

    private static final String TOKEN = "Bearer valid-test-token";
    private static final String EMAIL = "jane@example.com";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private TeamService teamService;
    @MockitoBean private TaskService taskService;
    @MockitoBean private TeamMemberRepository teamMemberRepository;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private JwtTokenProvider tokenProvider;
    @MockitoBean private UserDetailsServiceImpl userDetailsService;

    private User loggedInUser;

    @BeforeEach
    void logInAsJane() {
        loggedInUser = User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .username("jane")
                .password("hashed")
                .admin(true)
                .build();

        // Make the real JwtAuthenticationFilter accept our test token as Jane's
        when(tokenProvider.validateToken("valid-test-token")).thenReturn(true);
        when(tokenProvider.getEmailFromJWT("valid-test-token")).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername(EMAIL)
                        .password("hashed").roles("USER", "ADMIN").build());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(loggedInUser));
    }

    @Test
    void createTeamOwnerIsLoggedInUserEvenIfBodyNamesSomeoneElse() throws Exception {
        UUID someoneElse = UUID.randomUUID();

        mockMvc.perform(post("/api/teams")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Alpha\", \"ownerId\": \"" + someoneElse + "\"}"))
                .andExpect(status().isCreated());

        verify(teamService).createTeam(any(TeamDto.class), eq(loggedInUser));
    }

    @Test
    void createTaskCreatorIsLoggedInUserAndOldCreatorIdFieldIsIgnored() throws Exception {
        UUID listId = UUID.randomUUID();
        UUID someoneElse = UUID.randomUUID();

        // Older clients still send creatorId; it must be accepted but ignored
        mockMvc.perform(post("/api/lists/" + listId + "/tasks")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"Write report\", \"creatorId\": \"" + someoneElse + "\"}"))
                .andExpect(status().isCreated());

        verify(taskService).createTask(eq(listId), any(TaskDto.class), eq(loggedInUser));
    }

    @Test
    void addMemberAcceptsTheEmailTheFrontendFormSendsAndChecksTheLoggedInUser() throws Exception {
        UUID teamId = UUID.randomUUID();

        // Exactly the JSON the "Add Team Member" modal sends
        mockMvc.perform(post("/api/teams/" + teamId + "/members")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"bob@example.com\", \"role\": \"MEMBER\"}"))
                .andExpect(status().isCreated());

        verify(teamService).addMember(eq(teamId),
                argThat(dto -> "bob@example.com".equals(dto.getEmail())), eq(loggedInUser));
    }

    @Test
    void addMemberWithoutEmailIsRejectedWith400() throws Exception {
        mockMvc.perform(post("/api/teams/" + UUID.randomUUID() + "/members")
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"MEMBER\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTeamWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Alpha\"}"))
                .andExpect(status().isUnauthorized());
    }
}
