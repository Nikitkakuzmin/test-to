package kz.nik.testto.serviceTests;


import jakarta.persistence.EntityNotFoundException;
import kz.nik.testto.dto.AuthRequest;
import kz.nik.testto.dto.AuthResponse;
import kz.nik.testto.dto.RegisterRequest;
import kz.nik.testto.model.User;
import kz.nik.testto.repository.UserRepository;
import kz.nik.testto.service.AuthService;
import kz.nik.testto.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;



import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldCreateNewUser_WhenEmailIsUnique() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("mockToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(ResponseStatusException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_ShouldReturnToken_WhenCredentialsAreCorrect() {
        AuthRequest request = new AuthRequest("test@example.com", "password123");
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mockToken");

        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        AuthRequest request = new AuthRequest("notfound@example.com", "password123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.authenticate(request));
    }

    @Test
    void authenticate_ShouldThrowException_WhenCredentialsAreInvalid() {
        AuthRequest request = new AuthRequest("test@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> authService.authenticate(request));
    }
}