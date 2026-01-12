package com.banking.service;

import com.banking.dto.AccountDTO;
import com.banking.entity.Account;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Veritabanı tipini kontrol eder (SQLite için native SQL, H2 için normal save)
     */
    private boolean isSQLite() {
        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
            return databaseProductName.contains("sqlite");
        } catch (Exception e) {
            // Hata durumunda varsayılan olarak SQLite kabul et (production için)
            return true;
        }
    }

    @Transactional
    public Account createAccount(Long userId, Account.AccountType accountType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        String accountNumber = generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            accountNumber = generateAccountNumber();
        }

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(accountType);
        account.setUser(user);

        // SQLite için native SQL workaround, H2 için normal save()
        if (isSQLite()) {
            // SQLite JDBC driver doesn't support GeneratedKeys ResultSet properly
            // Workaround: Use native SQL with last_insert_rowid() to get the ID
            try {
                // Flush any pending changes before native query
                entityManager.flush();

                // Use native SQL INSERT to avoid getGeneratedKeys() issue
                String insertSql = "INSERT INTO accounts (account_number, balance, account_type, created_at, user_id) "
                        +
                        "VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?)";

                entityManager.createNativeQuery(insertSql)
                        .setParameter(1, account.getAccountNumber())
                        .setParameter(2, account.getBalance())
                        .setParameter(3, account.getAccountType().name())
                        .setParameter(4, user.getId())
                        .executeUpdate();

                // Flush to ensure INSERT is committed to database before querying
                // last_insert_rowid()
                entityManager.flush();

                // Get the last inserted ID using SQLite's last_insert_rowid()
                Long id = ((Number) entityManager.createNativeQuery("SELECT last_insert_rowid()")
                        .getSingleResult()).longValue();

                // Query the saved account with ID
                account = accountRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Hesap kaydedilemedi - ID alınamadı"));
                return account;
            } catch (Exception e) {
                // If native SQL fails, check if account was saved by accountNumber
                Account savedAccount = accountRepository.findByAccountNumber(accountNumber).orElse(null);
                if (savedAccount != null) {
                    return savedAccount;
                }
                throw new RuntimeException("Hesap kaydedilemedi: " + e.getMessage(), e);
            }
        } else {
            // H2 veya diğer veritabanları için normal save() kullan
            return accountRepository.save(account);
        }
    }

    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));
    }

    public List<AccountDTO> getUserAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return accountRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AccountDTO getAccountDTO(String accountNumber) {
        Account account = getAccountByNumber(accountNumber);
        return convertToDTO(account);
    }

    private AccountDTO convertToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        dto.setAccountType(account.getAccountType());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUserId(account.getUser().getId());
        dto.setFullName(account.getUser().getFirstName() + " " + account.getUser().getLastName());
        return dto;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        return String.format("%010d", random.nextInt(1000000000));
    }
}
