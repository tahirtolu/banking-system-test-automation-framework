package com.banking.dto;

import com.banking.entity.Account;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private Account.AccountType accountType;
    private LocalDateTime createdAt;
    private Long userId;
}

