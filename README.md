# transactions — Documentación del proyecto ✅

## Descripción breve
Aplicación demo en Spring Boot que gestiona transacciones simples en memoria.
- API REST ligera con endpoints para crear y consultar transacciones.
- Almacenamiento en memoria (`InMemoryTransactionRepository`) — no persistente.

---

## Ejecutar la aplicación 🚀
- Con Maven wrapper (Windows PowerShell / CMD):
```powershell
mvnw test           # Ejecuta la suite de tests
mvnw spring-boot:run  # Arranca la aplicación (puerto 8080 por defecto)
```
- Alternativa (jar):
```powershell
mvnw package
java -jar target\transactions-0.0.1-SNAPSHOT.jar
```

---

## Endpoints disponibles (resumen) 🔧
Todos los endpoints retornan/aceptan JSON.

### GET /transactions
Descripción: Devuelve todas las transacciones en el repositorio en formato {"transactions": {id: transaction,...}}
Ejemplo de respuesta:
```json
{
  "transactions": {
    "1": {"id":1,"type":"shopping","amount":100.0,"parentId":null}
  }
}
```

### GET /transactions/next-id
Descripción: Devuelve el próximo id disponible (estimado) en formato `{ "nextId": <number> }`.

### POST /transactions
Descripción: Crea una transacción. Request body (ejemplo):
```json
{ "type": "shopping", "amount": 100.0, "parentId": 1 }
```
Respuesta:
```json
{ "id": 1 }
```
Validaciones: si `parentId` está presente debe existir (si no existe, se lanza `IllegalArgumentException`). Además, `amount` debe ser no nulo y mayor o igual a 0; de lo contrario la creación fallará con `IllegalArgumentException`.

### GET /transactions/types
Descripción: Devuelve el set de tipos existentes. Ejemplo: `["shopping","food"]`.

### GET /transactions/types/{type}
Descripción: Devuelve lista de transacciones por tipo. Si `type` es `todos` devuelve todas.

### GET /transactions/{id}/sum
Descripción: Calcula la suma recursiva de `amount` de la transacción `id` incluyendo todas sus hijas (directas e indirectas). Respuesta de ejemplo:
```json
{ "sum": 150.0, "transactions": [ {...}, {...} ] }
```

---

## Modelo (`Transaction`)
- `Long id` — identificador
- `String type` — tipo de transacción
- `Double amount` — importe
- `Long parentId` — id de transacción padre (opcional)

> Nota: actualmente `Transaction` es un POJO simple; no se hacen validaciones avanzadas automáticas.

---

## Implementación de persistencia
- Clase principal: `InMemoryTransactionRepository` (implementa `TransactionRepository`)
  - Usa `ConcurrentHashMap<Long, Transaction>` para almacenar los objetos en memoria.
  - Genera ids con `AtomicLong`.
  - **Limitación:** todo se pierde al reiniciar la aplicación; para producción se recomienda reemplazar por JPA/DB.

---

## Tests 🧪
- Tests principales:
  - `TransactionControllerIntegrationTest` — pruebas de integración (flujo controller→service→repo) y casos de negocio.
  - `TransactionsApplicationTests` — test de arranque Spring Boot.

Comando para correr tests:
```powershell
mvnw test
```

---

## Qué cambiar para usar una base de datos real
1. Añadir dependencia `spring-boot-starter-data-jpa` y driver (`H2` para pruebas o `Postgres` en prod).
2. Marcar `Transaction` como `@Entity` o crear `TransactionEntity`.
3. Cambiar `TransactionRepository` para extender `JpaRepository<Transaction, Long>`.
4. Ajustar `TransactionService` para usar el repo JPA y manejar transacciones (`@Transactional`).
5. Añadir configuración en `application.properties` y migraciones (Flyway/Liquibase).

---

## Inspección desde el navegador
- Abrir `http://localhost:8080/` → verás `index.html` estático (si la app está corriendo).
- Endpoint `/transactions` devuelve JSON que podés inspeccionar desde DevTools (Network → Response).
- También podés usar `curl` o Postman para consumir los endpoints.
