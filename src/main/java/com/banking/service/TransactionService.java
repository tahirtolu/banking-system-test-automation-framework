package com.banking.service;

import com.banking.dto.DepositWithdrawalDTO;
import com.banking.dto.TransactionDTO;
import com.banking.dto.TransferDTO;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Transaction deposit(String accountNumber, DepositWithdrawalDTO dto) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));

        account.setBalance(account.getBalance().add(dto.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(dto.getAmount());
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setDescription(dto.getDescription() != null ? dto.getDescription() : "Para yatırma");

        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(String accountNumber, DepositWithdrawalDTO dto) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));

        if (account.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Yetersiz bakiye");
        }

        account.setBalance(account.getBalance().subtract(dto.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(dto.getAmount());
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setDescription(dto.getDescription() != null ? dto.getDescription() : "Para çekme");

        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction transfer(String fromAccountNumber, TransferDTO dto) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Gönderen hesap bulunamadı"));

        Account toAccount = accountRepository.findByAccountNumber(dto.getToAccountNumber())
                .orElseThrow(() -> new RuntimeException("Alıcı hesap bulunamadı"));

        if (fromAccount.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Yetersiz bakiye");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(dto.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(dto.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(dto.getAmount());
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setDescription(dto.getDescription() != null ? dto.getDescription() : "Para transferi");

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        return transactionRepository.save(transaction);
    }

    public List<TransactionDTO> getAccountTransactions(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));

        return transactionRepository.findByAccountOrderByTransactionDateDesc(account).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionNumber(transaction.getTransactionNumber());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        dto.setAccountId(transaction.getAccount().getId());
        if (transaction.getToAccount() != null) {
            dto.setToAccountNumber(transaction.getToAccount().getAccountNumber());
        }
        return dto;
    }
}

