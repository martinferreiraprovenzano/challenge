package com.example.transactions;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<Long, Transaction> store = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId(nextId.getAndIncrement());
        }
        store.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public Collection<Transaction> findAll() {
        return store.values();
    }

    @Override
    public Map<Long, Transaction> findAllAsMap() {
        return store;
    }

    @Override
    public Collection<Transaction> findByType(String type) {
        return store.values().stream()
                .filter(t -> type.equals(t.getType()))
                .collect(Collectors.toList());
    }
}
