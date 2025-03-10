package kz.nik.testto.serviceTests;


import kz.nik.testto.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secretKey = "mySuperSecretKeyForJWTThatIsLongEnoughForHS256";
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        jwtService = new JwtService(encodedKey);

        userDetails = new User("testUser", "password", List.of());
    }


    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("testUser", username);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenValidToken() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldThrowException_WhenUsernameMismatch() {
        UserDetails anotherUser = new User("anotherUser", "password", List.of());
        String token = jwtService.generateToken(userDetails);

        Exception exception = assertThrows(RuntimeException.class, () -> jwtService.validateToken(token, anotherUser));
        assertTrue(exception.getMessage().contains("Недействительный токен"));
    }

}