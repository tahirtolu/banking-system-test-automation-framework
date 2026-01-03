package com.banking.integration;

import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        accountRepository.deleteAll();

        user = new User();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("1234567890");
        user = userRepository.save(user);

        token = jwtUtil.generateToken(user.getUsername());
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        mockMvc.perform(post("/api/accounts")
                .param("accountType", "CHECKING")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.accountNumber").exists());
    }

    @Test
    void testGetUserAccounts_Success() throws Exception {
        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setBalance(java.math.BigDecimal.ZERO);
        account.setAccountType(Account.AccountType.CHECKING);
        account.setUser(user);
        accountRepository.save(account);

        mockMvc.perform(get("/api/accounts")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].accountNumber").exists());
    }

    @Test
    void testGetAccount_Success() throws Exception {
        Account account = new Account();
        account.setAccountNumber("1234567890");
        account.setBalance(java.math.BigDecimal.ZERO);
        account.setAccountType(Account.AccountType.CHECKING);
        account.setUser(user);
        account = accountRepository.save(account);

        mockMvc.perform(get("/api/accounts/" + account.getAccountNumber())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"));
    }

    @Test
    void testGetAccount_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/accounts/1234567890"))
                .andExpect(status().isForbidden());
    }
}

