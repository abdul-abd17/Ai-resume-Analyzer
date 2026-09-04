package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.UserDto;

public interface UserService {

    UserDto getUserProfile(String email);
}
