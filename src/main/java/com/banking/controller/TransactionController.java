package com.banking.controller;

import com.banking.dto.DepositWithdrawalDTO;
import com.banking.dto.TransactionDTO;
import com.banking.dto.TransferDTO;
import com.banking.entity.Transaction;
import com.banking.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<Map<String, Object>> deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositWithdrawalDTO dto) {
        try {
            Transaction transaction = transactionService.deposit(accountNumber, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Para yatırma işlemi başarılı");
            response.put("transactionNumber", transaction.getTransactionNumber());
            response.put("amount", transaction.getAmount());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositWithdrawalDTO dto) {
        try {
            Transaction transaction = transactionService.withdraw(accountNumber, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Para çekme işlemi başarılı");
            response.put("transactionNumber", transaction.getTransactionNumber());
            response.put("amount", transaction.getAmount());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{accountNumber}/transfer")
    public ResponseEntity<Map<String, Object>> transfer(
            @PathVariable String accountNumber,
            @Valid @RequestBody TransferDTO dto) {
        try {
            Transaction transaction = transactionService.transfer(accountNumber, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Para transferi başarılı");
            response.put("transactionNumber", transaction.getTransactionNumber());
            response.put("amount", transaction.getAmount());
            response.put("toAccountNumber", dto.getToAccountNumber());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{accountNumber}/history")
    public ResponseEntity<List<TransactionDTO>> getTransactionHistory(@PathVariable String accountNumber) {
        try {
            List<TransactionDTO> transactions = transactionService.getAccountTransactions(accountNumber);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

