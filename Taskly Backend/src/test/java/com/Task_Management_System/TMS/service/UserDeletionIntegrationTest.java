package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.model.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Deletes a user against the real PostgreSQL schema, so the database's foreign keys
 * are actually enforced (mocks can't catch a constraint violation).
 *
 * Runs inside a transaction that is rolled back afterwards, so no data is kept.
 * Needs PostgreSQL: excluded from the fast suite with -DexcludedGroups=database.
 */
@Tag("database")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // use PostgreSQL, not an in-memory DB
@Import(UserServiceImpl.class)
class UserDeletionIntegrationTest {

    @Autowired private UserServiceImpl userService;
    @Autowired private TestEntityManager em;

    @MockitoBean private PasswordEncoder passwordEncoder;

    @Test
    void deletingAUserWhoCreatedTasksAndSentJoinRequestsSucceeds() {
        // A team owned by an admin, with a member who has been active
        User owner = em.persist(user("owner", true));
        User leaver = em.persist(user("leaver", false));

        Team team = em.persist(Team.builder().name("Alpha").owner(owner).build());
        em.persist(TeamMember.builder().team(team).user(owner).role(Role.OWNER).build());
        em.persist(TeamMember.builder().team(team).user(leaver).role(Role.MEMBER).build());

        Board board = em.persist(Board.builder().name("Sprint").team(team).build());
        TaskList list = em.persist(TaskList.builder().name("To Do").position(0).board(board).build());

        // A task the leaver both created and is assigned to
        Task task = em.persist(Task.builder()
                .title("Write report")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now())
                .taskList(list)
                .creator(leaver)
                .assignee(leaver)
                .build());

        // A join request from the leaver to a second team, and the admin's notification about it
        Team otherTeam = em.persist(Team.builder().name("Beta").owner(owner).build());
        JoinRequest request = em.persist(JoinRequest.builder()
                .team(otherTeam).requester(leaver).admin(owner).status("PENDING").build());
        Notification adminNotice = em.persist(notification(owner, "REQUEST", request));
        Notification leaverNotice = em.persist(notification(leaver, "INFO", null));
        em.flush();
        em.clear();   // start with nothing cached, like a fresh HTTP request

        userService.deleteUser(leaver.getId(), owner);
        em.flush();   // send the deletes to PostgreSQL now, so any foreign-key violation fails the test
        em.clear();

        assertNull(em.find(User.class, leaver.getId()), "user is deleted");

        Task keptTask = em.find(Task.class, task.getId());
        assertNotNull(keptTask, "the team keeps the task");
        assertEquals(owner.getId(), keptTask.getCreator().getId(), "task is handed to the team owner");
        assertNull(keptTask.getAssignee(), "task is unassigned");

        assertNull(em.find(JoinRequest.class, request.getId()), "join request is removed");
        assertNull(em.find(Notification.class, adminNotice.getId()), "notification about the request is removed");
        assertNull(em.find(Notification.class, leaverNotice.getId()), "user's own notifications are removed");
    }

    private static User user(String name, boolean admin) {
        // Random suffix keeps emails unique even if the local database already has data
        String email = name + "-" + java.util.UUID.randomUUID() + "@example.com";
        return User.builder().username(name).email(email).password("hashed").admin(admin).build();
    }

    private static Notification notification(User recipient, String type, JoinRequest request) {
        return Notification.builder()
                .user(recipient)
                .title("Notice")
                .message("Something happened")
                .type(type)
                .requestId(request != null ? request.getId() : null)
                .build();
    }
}
