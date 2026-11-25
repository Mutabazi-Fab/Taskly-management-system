package com.Task_Management_System.TMS.controller;

import com.Task_Management_System.TMS.repository.UserRepository;
import com.Task_Management_System.TMS.security.JwtTokenProvider;
import com.Task_Management_System.TMS.security.SecurityConfig;
import com.Task_Management_System.TMS.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The Angular client is bundled into the jar (and the Docker image), so it has to load
 * before anyone logs in, while every /api route except login and sign-up stays protected.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class FrontendAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void appShellLoadsWithoutLogin() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void angularRoutesForwardToTheAppShellWithoutLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void apiStillRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isUnauthorized());
    }
}
