package com.company.banking.transfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

// ==========================================
// 1. CONTROLLER LAYER
// ==========================================
@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @PostMapping("/execute")
    public ResponseEntity<String> executeTransfer(@RequestParam Long fromId, 
                                                  @RequestParam Long toId, 
                                                  @RequestParam Double amount) {
        try {
            if (amount <= 0) {
                return ResponseEntity.badRequest().body("Amount must be positive");
            }
            String receipt = transferService.transferFunds(fromId, toId, amount);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            // Log error
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Transfer failed: " + e.getMessage());
        }
    }
}

// ==========================================
// 2. SERVICE LAYER
// ==========================================
@Service
public class TransferService {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;

    public String transferFunds(Long fromAccountId, Long toAccountId, Double amount) throws Exception {
        Account sender = accountRepository.findById(fromAccountId);
        Account receiver = accountRepository.findById(toAccountId);

        if (sender == null || receiver == null) {
            throw new Exception("Account not found");
        }

        if (sender.getBalance() < amount) {
            throw new Exception("Insufficient funds");
        }

        // Deduct from sender
        sender.setBalance(sender.getBalance() - amount);
        accountRepository.save(sender);

        // Simulate network call or external compliance check
        if (amount > 10000) {
            Thread.sleep(1000); // Heavy compliance check
        }

        // Add to receiver
        receiver.setBalance(receiver.getBalance() + amount);
        accountRepository.save(receiver);

        // Record audit log
        AuditLog log = new AuditLog(fromAccountId, toAccountId, amount, LocalDateTime.now());
        auditLogRepository.save(log);

        return "SUCCESS-" + UUID.randomUUID().toString();
    }
}

// ==========================================
// 3. DOMAIN MODELS & REPOSITORIES (Simulated)
// ==========================================
class Account {
    private Long id;
    private Double balance;

    public Account(Long id, Double balance) {
        this.id = id;
        this.balance = balance;
    }
    public Long getId() { return id; }
    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}

class AuditLog {
    private Long fromId;
    private Long toId;
    private Double amount;
    private LocalDateTime timestamp;

    public AuditLog(Long fromId, Long toId, Double amount, LocalDateTime timestamp) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}

interface AccountRepository {
    Account findById(Long id);
    void save(Account account);
}

interface AuditLogRepository {
    void save(AuditLog log);
}
