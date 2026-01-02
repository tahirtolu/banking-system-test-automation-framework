package com.banking.service;

import com.banking.dto.DepositWithdrawalDTO;
import com.banking.dto.TransferDTO;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account account;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setBalance(new BigDecimal("1000.00"));

        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber("0987654321");
        toAccount.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void testDeposit_Success() {
        DepositWithdrawalDTO dto = new DepositWithdrawalDTO();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("Test deposit");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Transaction result = transactionService.deposit("1234567890", dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("1100.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_Success() {
        DepositWithdrawalDTO dto = new DepositWithdrawalDTO();
        dto.setAmount(new BigDecimal("200.00"));
        dto.setDescription("Test withdrawal");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Transaction result = transactionService.withdraw("1234567890", dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        DepositWithdrawalDTO dto = new DepositWithdrawalDTO();
        dto.setAmount(new BigDecimal("2000.00"));

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> {
            transactionService.withdraw("1234567890", dto);
        });
    }

    @Test
    void testTransfer_Success() {
        TransferDTO dto = new TransferDTO();
        dto.setToAccountNumber("0987654321");
        dto.setAmount(new BigDecimal("300.00"));
        dto.setDescription("Test transfer");

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Transaction result = transactionService.transfer("1234567890", dto);

        assertNotNull(result);
        assertEquals(new BigDecimal("700.00"), account.getBalance());
        assertEquals(new BigDecimal("800.00"), toAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void testTransfer_InsufficientBalance() {
        TransferDTO dto = new TransferDTO();
        dto.setToAccountNumber("0987654321");
        dto.setAmount(new BigDecimal("2000.00"));

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(toAccount));

        assertThrows(RuntimeException.class, () -> {
            transactionService.transfer("1234567890", dto);
        });
    }

    @Test
    void testGetAccountTransactions_Success() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccount(account);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);

        List<Transaction> transactions = Arrays.asList(transaction);

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountOrderByTransactionDateDesc(account)).thenReturn(transactions);

        var result = transactionService.getAccountTransactions("1234567890");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

