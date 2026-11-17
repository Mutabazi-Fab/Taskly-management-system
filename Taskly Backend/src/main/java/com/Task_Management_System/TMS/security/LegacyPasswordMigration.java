package com.Task_Management_System.TMS.security;

import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

/**
 * One-time upgrade for accounts created before passwords were hashed.
 *
 * Earlier versions stored passwords as plain text. On startup this finds any
 * password that is not already a BCrypt hash and replaces it with one, so those
 * users can keep logging in with the same password. Already-hashed passwords
 * are left alone, so running it on every startup is safe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LegacyPasswordMigration implements ApplicationRunner {

    // BCrypt hashes look like: $2a$12$ followed by 53 characters of salt + hash
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$[./0-9A-Za-z]{53}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<User> legacyUsers = userRepository.findAll().stream()
                .filter(user -> !BCRYPT_PATTERN.matcher(user.getPassword()).matches())
                .toList();

        if (legacyUsers.isEmpty()) {
            return;
        }

        legacyUsers.forEach(user -> user.setPassword(passwordEncoder.encode(user.getPassword())));
        userRepository.saveAll(legacyUsers);
        log.info("Hashed {} legacy plain-text password(s) with BCrypt", legacyUsers.size());
    }
}
