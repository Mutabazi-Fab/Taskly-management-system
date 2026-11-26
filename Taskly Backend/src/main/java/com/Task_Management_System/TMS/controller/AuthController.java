package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.demo.DemoAccount;
import com.Task_Management_System.TMS.dto.AuthResponseDto;
import com.Task_Management_System.TMS.dto.DemoCredentialsDto;
import com.Task_Management_System.TMS.dto.LoginDto;
import com.Task_Management_System.TMS.model.User;
import com.Task_Management_System.TMS.repository.UserRepository;
import com.Task_Management_System.TMS.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Value("${app.demo.enabled:true}")
    private boolean demoEnabled;

    /** Public demo login shown on the login page. 404 when the demo is turned off. */
    @GetMapping("/demo")
    public ResponseEntity<DemoCredentialsDto> demoCredentials() {
        if (!demoEnabled) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DemoCredentialsDto(DemoAccount.EMAIL, DemoAccount.PASSWORD));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginDto.getEmail()).orElseThrow();
        String jwt = tokenProvider.generateToken(loginDto.getEmail(), user.isAdmin());

        return ResponseEntity.ok(AuthResponseDto.builder()
                .token(jwt)
                .email(user.getEmail())
                .username(user.getUsername())
                .userId(user.getId())
                .admin(user.isAdmin())
                .build());
    }
}
