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

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    private AccountService accountService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        // Mock DataSource to return H2 database (for tests, isSQLite() should return false)
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn("H2"); // H2, not SQLite
        } catch (Exception e) {
            // Ignore
        }

        // Create AccountService manually with all dependencies (including DataSource and EntityManager)
        // @InjectMocks doesn't handle @PersistenceContext fields
        accountService = new AccountService(accountRepository, userRepository, dataSource);
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
        
        // DataSource returns H2, so isSQLite() returns false, normal save() is used
        // No need to mock EntityManager for native SQL (H2 uses normal save())
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            a.setId(1L); // Set ID as if saved
            return a;
        });

        Account result = accountService.createAccount(1L, Account.AccountType.CHECKING);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // H2 uses normal save(), no native SQL queries should be made
        verify(accountRepository, times(1)).save(any(Account.class));
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

