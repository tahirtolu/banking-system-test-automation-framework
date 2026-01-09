package com.banking.service;

import com.banking.dto.DepositWithdrawalDTO;
import com.banking.dto.TransactionDTO;
import com.banking.dto.TransferDTO;
import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Transaction deposit(String accountNumber, DepositWithdrawalDTO dto) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));

        account.setBalance(account.getBalance().add(dto.getAmount()));
        accountRepository.save(account);

        // SQLite JDBC driver doesn't support GeneratedKeys ResultSet properly
        // Workaround: Use native SQL with last_insert_rowid() to get the ID
        try {
            entityManager.flush();
            
            // Generate unique transaction number (timestamp + random to avoid collisions)
            String transactionNumber = "TXN" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
            LocalDateTime transactionDate = LocalDateTime.now();
            String description = dto.getDescription() != null ? dto.getDescription() : "Para yatırma";
            
            String insertSql = "INSERT INTO transactions (transaction_number, amount, transaction_type, transaction_date, description, account_id, to_account_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NULL)";
            
            entityManager.createNativeQuery(insertSql)
                    .setParameter(1, transactionNumber)
                    .setParameter(2, dto.getAmount())
                    .setParameter(3, Transaction.TransactionType.DEPOSIT.name())
                    .setParameter(4, transactionDate)
                    .setParameter(5, description)
                    .setParameter(6, account.getId())
                    .executeUpdate();
            
            entityManager.flush();
            
            Long id = ((Number) entityManager.createNativeQuery("SELECT last_insert_rowid()")
                    .getSingleResult()).longValue();
            
            Transaction transaction = transactionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("İşlem kaydedilemedi - ID alınamadı"));
            
            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("İşlem kaydedilemedi: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Transaction withdraw(String accountNumber, DepositWithdrawalDTO dto) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));

        if (account.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new RuntimeException("Yetersiz bakiye");
        }

        account.setBalance(account.getBalance().subtract(dto.getAmount()));
        accountRepository.save(account);

        // SQLite JDBC driver doesn't support GeneratedKeys ResultSet properly
        // Workaround: Use native SQL with last_insert_rowid() to get the ID
        try {
            entityManager.flush();
            
            // Generate unique transaction number (timestamp + random to avoid collisions)
            String transactionNumber = "TXN" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
            LocalDateTime transactionDate = LocalDateTime.now();
            String description = dto.getDescription() != null ? dto.getDescription() : "Para çekme";
            
            String insertSql = "INSERT INTO transactions (transaction_number, amount, transaction_type, transaction_date, description, account_id, to_account_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NULL)";
            
            entityManager.createNativeQuery(insertSql)
                    .setParameter(1, transactionNumber)
                    .setParameter(2, dto.getAmount())
                    .setParameter(3, Transaction.TransactionType.WITHDRAWAL.name())
                    .setParameter(4, transactionDate)
                    .setParameter(5, description)
                    .setParameter(6, account.getId())
                    .executeUpdate();
            
            entityManager.flush();
            
            Long id = ((Number) entityManager.createNativeQuery("SELECT last_insert_rowid()")
                    .getSingleResult()).longValue();
            
            Transaction transaction = transactionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("İşlem kaydedilemedi - ID alınamadı"));
            
            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("İşlem kaydedilemedi: " + e.getMessage(), e);
        }
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

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // SQLite JDBC driver doesn't support GeneratedKeys ResultSet properly
        // Workaround: Use native SQL with last_insert_rowid() to get the ID
        try {
            entityManager.flush();
            
            // Generate unique transaction number (timestamp + random to avoid collisions)
            String transactionNumber = "TXN" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
            LocalDateTime transactionDate = LocalDateTime.now();
            String description = dto.getDescription() != null ? dto.getDescription() : "Para transferi";
            
            String insertSql = "INSERT INTO transactions (transaction_number, amount, transaction_type, transaction_date, description, account_id, to_account_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            entityManager.createNativeQuery(insertSql)
                    .setParameter(1, transactionNumber)
                    .setParameter(2, dto.getAmount())
                    .setParameter(3, Transaction.TransactionType.TRANSFER.name())
                    .setParameter(4, transactionDate)
                    .setParameter(5, description)
                    .setParameter(6, fromAccount.getId())
                    .setParameter(7, toAccount.getId())
                    .executeUpdate();
            
            entityManager.flush();
            
            Long id = ((Number) entityManager.createNativeQuery("SELECT last_insert_rowid()")
                    .getSingleResult()).longValue();
            
            Transaction transaction = transactionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("İşlem kaydedilemedi - ID alınamadı"));
            
            return transaction;
        } catch (Exception e) {
            throw new RuntimeException("İşlem kaydedilemedi: " + e.getMessage(), e);
        }
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

