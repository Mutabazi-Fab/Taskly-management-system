package com.Task_Management_System.TMS.service;

import com.Task_Management_System.TMS.dto.UserRegistrationDto;
import com.Task_Management_System.TMS.dto.UserResponseDto;
import com.Task_Management_System.TMS.dto.UserUpdateDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDto createUser(UserRegistrationDto registrationDto);
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(UUID id);
    UserResponseDto updateUser(UUID id, UserUpdateDto updateDto);
    void deleteUser(UUID id);
    UserResponseDto toggleAdminStatus(UUID id);
}
