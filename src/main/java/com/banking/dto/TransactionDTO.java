package com.banking.dto;

import com.banking.entity.Transaction;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;
    private String transactionNumber;
    private BigDecimal amount;
    private Transaction.TransactionType transactionType;
    private LocalDateTime transactionDate;
    private String description;
    private Long accountId;
    private String toAccountNumber;
}

