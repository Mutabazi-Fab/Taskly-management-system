package com.Task_Management_System.TMS.demo;

import com.Task_Management_System.TMS.model.Board;
import com.Task_Management_System.TMS.model.Role;
import com.Task_Management_System.TMS.model.Task;
import com.Task_Management_System.TMS.model.TaskList;
import com.Task_Management_System.TMS.model.TaskPriority;
import com.Task_Management_System.TMS.model.TaskStatus;
import com.Task_Management_System.TMS.model.Team;
import com.Task_Management_System.TMS.model.TeamMember;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.BoardRepository;
import com.Task_Management_System.TMS.repository.TaskListRepository;
import com.Task_Management_System.TMS.repository.TaskRepository;
import com.Task_Management_System.TMS.repository.TeamMemberRepository;
import com.Task_Management_System.TMS.repository.TeamRepository;
import com.Task_Management_System.TMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Creates a ready-to-explore workspace (demo user, teammates, a board and tasks) the first time
 * the app starts, so visitors can click "Try the live demo" instead of building data by hand.
 * Runs only once: if the demo user already exists, nothing is touched.
 * Turn it off with DEMO_ENABLED=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final BoardRepository boardRepository;
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(DemoAccount.EMAIL).isPresent()) {
            return;
        }

        User demo = createUser(DemoAccount.USERNAME, DemoAccount.EMAIL, DemoAccount.PASSWORD);
        // Teammates fill the workload roster. Their passwords are random, so nobody can log in as them.
        User jordan = createUser("Jordan Lee", "jordan@demo.taskly.dev", UUID.randomUUID().toString());
        User sam = createUser("Sam Rivera", "sam@demo.taskly.dev", UUID.randomUUID().toString());

        Team team = teamRepository.save(Team.builder().name("Launch Squad").owner(demo).build());
        addMember(team, demo, Role.OWNER);
        addMember(team, jordan, Role.MEMBER);
        addMember(team, sam, Role.MEMBER);

        Board board = boardRepository.save(Board.builder().name("Product Launch").team(team).build());
        TaskList todo = createList(board, "To Do", 0);
        TaskList inProgress = createList(board, "In Progress", 1);
        TaskList done = createList(board, "Done", 2);

        LocalDate today = LocalDate.now();

        createTask(todo, demo, demo, "Write launch announcement",
                "Draft the blog post and social copy for launch day.", TaskPriority.HIGH, today.plusDays(1));
        createTask(todo, demo, sam, "Prepare demo video",
                "Two-minute walkthrough of boards, calendar and reports.", TaskPriority.MEDIUM, today.plusDays(5));
        createTask(todo, demo, jordan, "Set up status page",
                "Public page showing uptime for the API.", TaskPriority.LOW, today.plusDays(10));
        createTask(todo, demo, demo, "Collect beta feedback",
                "Summarise the survey answers from the beta group.", TaskPriority.HIGH, today.minusDays(2));

        createTask(inProgress, demo, demo, "Polish onboarding flow",
                "Shorten sign-up to one screen and add a welcome tour.", TaskPriority.HIGH, today);
        createTask(inProgress, demo, jordan, "Fix mobile layout bugs",
                "Calendar and board views overflow on small phones.", TaskPriority.MEDIUM, today.plusDays(3));
        createTask(inProgress, demo, sam, "Load-test the API",
                "Target 500 concurrent users on the board endpoints.", TaskPriority.MEDIUM, today.plusDays(4));

        createTask(done, demo, demo, "Design the logo",
                "Final files exported in SVG and PNG.", TaskPriority.MEDIUM, today.minusDays(6));
        createTask(done, demo, jordan, "Set up database backups",
                "Nightly backups with 14-day retention.", TaskPriority.HIGH, today.minusDays(4));
        createTask(done, demo, sam, "Choose pricing tiers",
                "Free, Team and Business plans agreed.", TaskPriority.LOW, today.minusDays(1));

        log.info("Demo workspace created. Log in with {} / {}", DemoAccount.EMAIL, DemoAccount.PASSWORD);
    }

    private User createUser(String username, String email, String rawPassword) {
        return userRepository.findByEmail(email).orElseGet(() -> userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .admin(false)
                .build()));
    }

    private void addMember(Team team, User user, Role role) {
        teamMemberRepository.save(TeamMember.builder().team(team).user(user).role(role).build());
    }

    private TaskList createList(Board board, String name, int position) {
        return taskListRepository.save(TaskList.builder().name(name).position(position).board(board).build());
    }

    private void createTask(TaskList list, User creator, User assignee, String title, String description,
                            TaskPriority priority, LocalDate dueDate) {
        taskRepository.save(Task.builder()
                .title(title)
                .description(description)
                .status(statusFor(list))
                .priority(priority)
                .dueDate(dueDate)
                .taskList(list)
                .creator(creator)
                .assignee(assignee)
                .build());
    }

    private static TaskStatus statusFor(TaskList list) {
        return switch (list.getPosition()) {
            case 0 -> TaskStatus.TODO;
            case 1 -> TaskStatus.IN_PROGRESS;
            default -> TaskStatus.DONE;
        };
    }
}
