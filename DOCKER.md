# 🐳 COMANDOS DOCKER PARA LA APLICACIÓN

## Construir y ejecutar:
```bash
# 1. Compilar aplicación
.\mvnw.cmd clean package -DskipTests

# 2. Construir imagen Docker
docker build -t transactions-app .

# 3. Ejecutar contenedor
docker run -d -p 8080:8080 --name transactions-container transactions-app
```

## Comandos útiles:
```bash
# Ver contenedores ejecutándose
docker ps

# Ver logs del contenedor
docker logs transactions-container

# Detener contenedor
docker stop transactions-container

# Eliminar contenedor
docker rm transactions-container

# Eliminar imagen
docker rmi transactions-app
```

## Acceso:
- Aplicación: http://localhost:8080
- API REST: http://localhost:8080/transactions