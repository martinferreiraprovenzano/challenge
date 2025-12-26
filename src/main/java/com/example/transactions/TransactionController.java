package com.example.transactions;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
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
        if (transaction.getParentId() != null && !transactions.containsKey(transaction.getParentId())) {
            throw new IllegalArgumentException("Parent ID does not exist");
        }
        transaction.setId(nextId);
        transactions.put(nextId, transaction);
        return Map.of("id", nextId++);
    }
    
    @GetMapping("/transactions/types")
    public Set<String> getAllTypes() {
        return transactions.values().stream()
                .map(Transaction::getType)
                .collect(Collectors.toSet());
    }
    
    @GetMapping("/transactions/types/{type}")
    public List<Transaction> getTransactionsByType(@PathVariable String type) {
        if ("todos".equals(type)) {
            return transactions.values().stream().collect(Collectors.toList());
        }
        return transactions.values().stream()
                .filter(t -> type.equals(t.getType()))
                .collect(Collectors.toList());
    }
    
    @GetMapping("/transactions/{id}/sum")
    public Map<String, Object> getSum(@PathVariable Long id) {
        double sum = calculateSum(id);
        List<Transaction> relatedTransactions = getRelatedTransactions(id);
        return Map.of("sum", sum, "transactions", relatedTransactions);
    }
    
    private List<Transaction> getRelatedTransactions(Long id) {
        return transactions.values().stream()
                .filter(t -> isRelated(t, id))
                .collect(Collectors.toList());
    }
    
    private boolean isRelated(Transaction t, Long targetId) {
        if (targetId.equals(t.getId())) return true;
        if (targetId.equals(t.getParentId())) return true;
        return hasParent(t.getId(), targetId);
    }
    
    private boolean hasParent(Long childId, Long targetParentId) {
        Transaction child = transactions.get(childId);
        if (child == null || child.getParentId() == null) return false;
        if (targetParentId.equals(child.getParentId())) return true;
        return hasParent(child.getParentId(), targetParentId);
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