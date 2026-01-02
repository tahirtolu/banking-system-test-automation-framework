package com.banking.integration;

import com.banking.dto.DepositWithdrawalDTO;
import com.banking.dto.TransferDTO;
import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import com.banking.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private User user;
    private Account account;
    private Account toAccount;
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

        account = new Account();
        account.setAccountNumber("1234567890");
        account.setBalance(new BigDecimal("1000.00"));
        account.setAccountType(Account.AccountType.CHECKING);
        account.setUser(user);
        account = accountRepository.save(account);

        toAccount = new Account();
        toAccount.setAccountNumber("0987654321");
        toAccount.setBalance(new BigDecimal("500.00"));
        toAccount.setAccountType(Account.AccountType.SAVINGS);
        toAccount.setUser(user);
        toAccount = accountRepository.save(toAccount);

        token = jwtUtil.generateToken(user.getUsername());
    }

    @Test
    void testDeposit_Success() throws Exception {
        DepositWithdrawalDTO dto = new DepositWithdrawalDTO();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("Test deposit");

        mockMvc.perform(post("/api/transactions/" + account.getAccountNumber() + "/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.transactionNumber").exists());
    }

    @Test
    void testWithdraw_Success() throws Exception {
        DepositWithdrawalDTO dto = new DepositWithdrawalDTO();
        dto.setAmount(new BigDecimal("200.00"));
        dto.setDescription("Test withdrawal");

        mockMvc.perform(post("/api/transactions/" + account.getAccountNumber() + "/withdraw")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testWithdraw_InsufficientBalance() throws Exception {
        DepositWithdrawalDTO dto = new DepositWithdrawalDTO();
        dto.setAmount(new BigDecimal("2000.00"));

        mockMvc.perform(post("/api/transactions/" + account.getAccountNumber() + "/withdraw")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testTransfer_Success() throws Exception {
        TransferDTO dto = new TransferDTO();
        dto.setToAccountNumber(toAccount.getAccountNumber());
        dto.setAmount(new BigDecimal("300.00"));
        dto.setDescription("Test transfer");

        mockMvc.perform(post("/api/transactions/" + account.getAccountNumber() + "/transfer")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGetTransactionHistory_Success() throws Exception {
        mockMvc.perform(get("/api/transactions/" + account.getAccountNumber() + "/history")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

