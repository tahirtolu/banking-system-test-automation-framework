package com.banking.service;

import com.banking.dto.LoginDTO;
import com.banking.dto.UserRegistrationDTO;
import com.banking.entity.User;
import com.banking.repository.UserRepository;
import com.banking.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    private UserService userService;

    private UserRegistrationDTO registrationDTO;
    private User user;

    @BeforeEach
    void setUp() {
        // Create UserService manually with all dependencies (including EntityManager)
        // @InjectMocks doesn't handle @PersistenceContext fields
        userService = new UserService(userRepository, passwordEncoder, jwtUtil);
        ReflectionTestUtils.setField(userService, "entityManager", entityManager);
        
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("testuser");
        registrationDTO.setPassword("password123");
        registrationDTO.setEmail("test@example.com");
        registrationDTO.setFirstName("Test");
        registrationDTO.setLastName("User");
        registrationDTO.setPhoneNumber("1234567890");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("1234567890");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        // Mock EntityManager for native SQL queries
        // First query: INSERT statement
        Query insertQuery = mock(Query.class);
        when(insertQuery.setParameter(anyInt(), any())).thenReturn(insertQuery);
        when(insertQuery.executeUpdate()).thenReturn(1);
        
        // Second query: SELECT last_insert_rowid()
        Query selectQuery = mock(Query.class);
        when(selectQuery.getSingleResult()).thenReturn(1L);
        
        // Return appropriate query based on SQL string
        when(entityManager.createNativeQuery(contains("INSERT"))).thenReturn(insertQuery);
        when(entityManager.createNativeQuery(contains("SELECT last_insert_rowid"))).thenReturn(selectQuery);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.registerUser(registrationDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(1L, result.getId());
        verify(entityManager, atLeastOnce()).createNativeQuery(anyString());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testRegisterUser_UsernameExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDTO);
        });
    }

    @Test
    void testRegisterUser_EmailExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.registerUser(registrationDTO);
        });
    }

    @Test
    void testLogin_Success() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwtToken");

        String token = userService.login(loginDTO);

        assertNotNull(token);
        assertEquals("jwtToken", token);
    }

    @Test
    void testLogin_UserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });
    }

    @Test
    void testLogin_WrongPassword() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("wrongPassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            userService.login(loginDTO);
        });
    }
}

