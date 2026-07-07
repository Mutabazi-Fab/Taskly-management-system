package com.Task_Management_System.TMS.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
    private String token;
    private String email;
    private String username;
    private UUID userId;
    private boolean admin;
}
