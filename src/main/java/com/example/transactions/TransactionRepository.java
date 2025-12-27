package com.example.transactions;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    boolean existsById(Long id);
    Collection<Transaction> findAll();
    Map<Long, Transaction> findAllAsMap();
    Collection<Transaction> findByType(String type);
}
