package com.banking.controller;

import com.banking.dto.AccountDTO;
import com.banking.entity.Account;
import com.banking.service.AccountService;
import com.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(
            @RequestParam Account.AccountType accountType,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.findByUsername(username).getId();
            Account account = accountService.createAccount(userId, accountType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Hesap başarıyla oluşturuldu");
            response.put("accountNumber", account.getAccountNumber());
            response.put("accountType", account.getAccountType());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getUserAccounts(Authentication authentication) {
        try {
            String username = authentication.getName();
            Long userId = userService.findByUsername(username).getId();
            List<AccountDTO> accounts = accountService.getUserAccounts(userId);
            return ResponseEntity.ok(accounts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountNumber) {
        try {
            AccountDTO account = accountService.getAccountDTO(accountNumber);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

