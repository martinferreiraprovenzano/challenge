package com.example.transactions;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    // No-arg constructor for tests or manual instantiation
    public TransactionController() {
        this(new TransactionService(new InMemoryTransactionRepository()));
    }

    @GetMapping("/transactions")
    public Map<String, Object> getAllTransactions() {
        return service.getAllTransactions();
    }

    @GetMapping("/transactions/next-id")
    public Map<String, Long> getNextId() {
        return service.getNextId();
    }

    @PostMapping("/transactions")
    public Map<String, Long> createTransaction(@RequestBody Transaction transaction) {
        Long id = service.createTransaction(transaction);
        return Map.of("id", id);
    }

    @GetMapping("/transactions/types")
    public Set<String> getAllTypes() {
        return service.getAllTypes();
    }

    @GetMapping("/transactions/types/{type}")
    public List<Transaction> getTransactionsByType(@PathVariable String type) {
        return service.getTransactionsByType(type);
    }

    @GetMapping("/transactions/{id}/sum")
    public Map<String, Object> getSum(@PathVariable Long id) {
        return service.getSum(id);
    }
}