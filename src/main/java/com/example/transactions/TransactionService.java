package com.example.transactions;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionService {
    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Map<String, Object> getAllTransactions() {
        return Map.of("transactions", repository.findAllAsMap());
    }

    public Long createTransaction(Transaction transaction) {
        // Validate parent exists
        if (transaction.getParentId() != null && !repository.existsById(transaction.getParentId())) {
            throw new IllegalArgumentException("Parent ID does not exist");
        }
        // Validate amount is provided and non-negative
        if (transaction.getAmount() == null || transaction.getAmount() < 0.0) {
            throw new IllegalArgumentException("Amount must be non-null and >= 0");
        }
        Transaction saved = repository.save(transaction);
        return saved.getId();
    }

    public Map<String, Long> getNextId() {
        // Not ideal to expose repository internals; approximate by using current max + 1
        long nextId = repository.findAllAsMap().keySet().stream().mapToLong(k -> k).max().orElse(0L) + 1;
        return Map.of("nextId", nextId);
    }

    public Set<String> getAllTypes() {
        Collection<Transaction> all = repository.findAll();
        Set<String> types = new HashSet<>();
        for (Transaction t : all) {
            if (t.getType() != null) types.add(t.getType());
        }
        return types;
    }

    public List<Transaction> getTransactionsByType(String type) {
        if ("todos".equals(type)) {
            return new ArrayList<>(repository.findAll());
        }
        return new ArrayList<>(repository.findByType(type));
    }

    public Map<String, Object> getSum(Long id) {
        double sum = calculateSum(id);
        List<Transaction> related = getRelatedTransactions(id);
        return Map.of("sum", sum, "transactions", related);
    }

    private List<Transaction> getRelatedTransactions(Long id) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : repository.findAll()) {
            if (isRelated(t, id)) result.add(t);
        }
        return result;
    }

    private boolean isRelated(Transaction t, Long targetId) {
        if (t == null) return false;
        if (targetId.equals(t.getId())) return true;
        if (targetId.equals(t.getParentId())) return true;
        return hasParent(t.getId(), targetId);
    }

    private boolean hasParent(Long childId, Long targetParentId) {
        Optional<Transaction> childOpt = repository.findById(childId);
        if (childOpt.isEmpty()) return false;
        Transaction child = childOpt.get();
        if (child.getParentId() == null) return false;
        if (targetParentId.equals(child.getParentId())) return true;
        return hasParent(child.getParentId(), targetParentId);
    }

    private double calculateSum(Long id) {
        Optional<Transaction> opt = repository.findById(id);
        if (opt.isEmpty()) return 0;
        Transaction tx = opt.get();
        double sum = tx.getAmount() == null ? 0.0 : tx.getAmount();
        for (Transaction t : repository.findAll()) {
            if (id.equals(t.getParentId())) {
                sum += calculateSum(t.getId());
            }
        }
        return sum;
    }
}
