package com.example.transactions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransactionControllerIntegrationTest {

    @Test
    public void testTransactionController() {
        System.out.println("\n=== INICIANDO TEST DE INTEGRACIÓN ===");
        TransactionController controller = new TransactionController();
        
        // Test 1: Create parent transaction
        System.out.println("\n1. Creando transacción padre (shopping, $100)");
        Transaction parent = new Transaction();
        parent.setType("shopping");
        parent.setAmount(100.0);
        
        var result1 = controller.createTransaction(parent);
        assertEquals(1L, result1.get("id"));
        System.out.println("   ✅ Transacción creada con ID: " + result1.get("id"));
        
        // Test 2: Create child transaction
        System.out.println("\n2. Creando transacción hija (food, $50, parentId=1)");
        Transaction child = new Transaction();
        child.setType("food");
        child.setAmount(50.0);
        child.setParentId(1L);
        
        var result2 = controller.createTransaction(child);
        assertEquals(2L, result2.get("id"));
        System.out.println("   ✅ Transacción creada con ID: " + result2.get("id"));
        
        // Test 3: Test sum calculation
        System.out.println("\n3. Calculando suma de transacción ID=1");
        var sumResult = controller.getSum(1L);
        assertEquals(150.0, sumResult.get("sum"));
        System.out.println("   ✅ Suma calculada: $" + sumResult.get("sum"));
        System.out.println("   📋 Transacciones incluidas: " + ((java.util.List)sumResult.get("transactions")).size());
        
        // Test 4: Get transactions by type
        System.out.println("\n4. Buscando transacciones tipo 'shopping'");
        var typeResult = controller.getTransactionsByType("shopping");
        assertEquals(1, typeResult.size());
        assertEquals("shopping", typeResult.get(0).getType());
        System.out.println("   ✅ Encontradas: " + typeResult.size() + " transacciones");
        
        // Test 5: Get all types
        System.out.println("\n5. Obteniendo todos los tipos");
        var allTypes = controller.getAllTypes();
        assertTrue(allTypes.contains("shopping"));
        assertTrue(allTypes.contains("food"));
        System.out.println("   ✅ Tipos encontrados: " + allTypes);
        
        // Test 6: Invalid parent ID should throw exception
        System.out.println("\n6. Probando parentId inválido (999)");
        Transaction invalid = new Transaction();
        invalid.setType("invalid");
        invalid.setAmount(10.0);
        invalid.setParentId(999L);
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.createTransaction(invalid);
        });
        System.out.println("   ✅ Error capturado correctamente: Parent ID no existe");
        
        // Test 7: Get next ID
        System.out.println("\n7. Verificando próximo ID");
        var nextIdResult = controller.getNextId();
        assertEquals(3L, nextIdResult.get("nextId"));
        System.out.println("   ✅ Próximo ID: " + nextIdResult.get("nextId"));
        
        System.out.println("\n=== TEST COMPLETADO EXITOSAMENTE ===");
    }

    @Test
    public void repositorySaveAndFind() {
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();

        Transaction t1 = new Transaction();
        t1.setType("shopping");
        t1.setAmount(100.0);

        Transaction saved1 = repo.save(t1);
        assertNotNull(saved1.getId());
        assertTrue(repo.existsById(saved1.getId()));

        Transaction t2 = new Transaction();
        t2.setType("food");
        t2.setAmount(50.0);
        t2.setParentId(saved1.getId());

        Transaction saved2 = repo.save(t2);
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());

        java.util.Collection<Transaction> all = repo.findAll();
        assertEquals(2, all.size());

        assertEquals(1, repo.findByType("shopping").size());
        assertEquals(1, repo.findByType("food").size());
    }

    @Test
    public void serviceCreateAndSumAndTypes() {
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();
        TransactionService service = new TransactionService(repo);

        Transaction parent = new Transaction();
        parent.setType("shopping");
        parent.setAmount(100.0);

        Long id1 = service.createTransaction(parent);
        assertEquals(1L, id1);

        Transaction child = new Transaction();
        child.setType("food");
        child.setAmount(50.0);
        child.setParentId(id1);

        Long id2 = service.createTransaction(child);
        assertEquals(2L, id2);

        Map<String, Object> sumResult = service.getSum(id1);
        assertEquals(150.0, (Double)sumResult.get("sum"));

        List<Transaction> shopping = service.getTransactionsByType("shopping");
        assertEquals(1, shopping.size());

        Set<String> types = service.getAllTypes();
        assertTrue(types.contains("shopping"));
        assertTrue(types.contains("food"));

        Transaction invalid = new Transaction();
        invalid.setType("invalid");
        invalid.setAmount(10.0);
        invalid.setParentId(999L);

        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(invalid));

        // Test invalid amounts (null and negative)
        Transaction noAmount = new Transaction();
        noAmount.setType("none");
        // amount left null
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(noAmount));

        Transaction negative = new Transaction();
        negative.setType("bad");
        negative.setAmount(-5.0);
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(negative));

        Map<String, Long> next = service.getNextId();
        assertEquals(3L, next.get("nextId"));
    }
}