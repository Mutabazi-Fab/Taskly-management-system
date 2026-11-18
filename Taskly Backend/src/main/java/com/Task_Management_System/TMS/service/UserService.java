package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.UserRegistrationDto;
import com.Task_Management_System.TMS.dto.UserResponseDto;
import com.Task_Management_System.TMS.dto.UserUpdateDto;
import com.Task_Management_System.TMS.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDto createUser(UserRegistrationDto registrationDto);
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(UUID id);

    // actingUser = the logged-in user making the request, used for permission checks
    UserResponseDto updateUser(UUID id, UserUpdateDto updateDto, User actingUser);
    void deleteUser(UUID id, User actingUser);
    UserResponseDto toggleAdminStatus(UUID id, User actingUser);
}
