package com.example.transactions;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TransactionController {
    
    private Map<Long, Transaction> transactions = new HashMap<>();
    private Long nextId = 1L;
    
    @GetMapping("/transactions")
    public Map<String, Object> getAllTransactions() {
        return Map.of("transactions", transactions);
    }
    
    @GetMapping("/transactions/next-id")
    public Map<String, Long> getNextId() {
        return Map.of("nextId", nextId);
    }
    
    @PostMapping("/transactions")
    public Map<String, Long> createTransaction(@RequestBody Transaction transaction) {
        transaction.setId(nextId);
        transactions.put(nextId, transaction);
        return Map.of("id", nextId++);
    }
    
    @GetMapping("/transactions/types/{type}")
    public List<Long> getTransactionsByType(@PathVariable String type) {
        return transactions.values().stream()
                .filter(t -> type.equals(t.getType()))
                .map(Transaction::getId)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/transactions/{id}/sum")
    public Map<String, Double> getSum(@PathVariable Long id) {
        double sum = calculateSum(id);
        return Map.of("sum", sum);
    }
    
    private double calculateSum(Long id) {
        Transaction transaction = transactions.get(id);
        if (transaction == null) return 0;
        
        double sum = transaction.getAmount();
        for (Transaction t : transactions.values()) {
            if (id.equals(t.getParentId())) {
                sum += calculateSum(t.getId());
            }
        }
        return sum;
    }
}