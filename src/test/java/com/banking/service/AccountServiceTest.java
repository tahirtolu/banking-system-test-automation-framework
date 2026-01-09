package com.banking.service;

import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    private AccountService accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        // Create AccountService manually with all dependencies (including EntityManager)
        // @InjectMocks doesn't handle @PersistenceContext fields
        accountService = new AccountService(accountRepository, userRepository);
        ReflectionTestUtils.setField(accountService, "entityManager", entityManager);
        
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(Account.AccountType.CHECKING);
        account.setUser(user);
    }

    @Test
    void testCreateAccount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        
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
        
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account result = accountService.createAccount(1L, Account.AccountType.CHECKING);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(entityManager, atLeastOnce()).createNativeQuery(anyString());
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateAccount_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            accountService.createAccount(1L, Account.AccountType.CHECKING);
        });
    }

    @Test
    void testGetAccountByNumber_Success() {
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));

        Account result = accountService.getAccountByNumber("1234567890");

        assertNotNull(result);
        assertEquals("1234567890", result.getAccountNumber());
    }

    @Test
    void testGetAccountByNumber_NotFound() {
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            accountService.getAccountByNumber("1234567890");
        });
    }

    @Test
    void testGetUserAccounts_Success() {
        List<Account> accounts = Arrays.asList(account);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByUser(user)).thenReturn(accounts);

        var result = accountService.getUserAccounts(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

