package com.airesume.analyzer.service;

import com.airesume.analyzer.dto.AuthResponse;
import com.airesume.analyzer.dto.RegisterRequest;
import com.airesume.analyzer.dto.UserDto;
import com.airesume.analyzer.entity.Role;
import com.airesume.analyzer.entity.User;
import com.airesume.analyzer.exception.DuplicateEmailException;
import com.airesume.analyzer.mapper.UserMapper;
import com.airesume.analyzer.repository.UserRepository;
import com.airesume.analyzer.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test Architect");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("SecretPassword123!");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .fullName("Test Architect")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .status("ACTIVE")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toDto(any())).thenReturn(UserDto.builder().id(1L).email("test@example.com").build());
        when(tokenProvider.generateToken(any())).thenReturn("mockedJwtToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mockedJwtToken", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(registerRequest));
    }
}
