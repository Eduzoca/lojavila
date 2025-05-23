# Loja Vila

Aplicação desktop Java Swing para gerenciamento de uma loja simples, com produtos, vendas e relatórios. Utiliza Maven, MySQL e FlatLaf para o look-and-feel.

---

## Pré-requisitos

- **Java 24** (JDK 24) instalado e `JAVA_HOME` configurado.
- **Maven 3.6+** instalado e no `PATH`.
- **MySQL** ou **MariaDB** rodando localmente.
- Conector JDBC MySQL (incluso pelo Maven).

---

## 1. Configurar o banco de dados

1. Crie um schema chamado `lojavila` no seu servidor MySQL:
    ```sql
    CREATE DATABASE lojavila CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
    ```

2. No prompt `mysql`, rode o script `schema.sql` (veja abaixo) para criar tabelas e popular dados de exemplo:

    ```sql
    -- Limpeza
    DROP TABLE IF EXISTS sales;
    DROP TABLE IF EXISTS products;
    DROP TABLE IF EXISTS users;

    -- Usuários
    CREATE TABLE users (
      id INT AUTO_INCREMENT PRIMARY KEY,
      username VARCHAR(50) NOT NULL UNIQUE,
      password_hash VARCHAR(255) NOT NULL,
      role ENUM('ADMIN','EMPLOYEE') NOT NULL
    );

    -- Produtos
    CREATE TABLE products (
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(100) NOT NULL,
      price DECIMAL(10,2) NOT NULL,
      quantity INT NOT NULL DEFAULT 0
    );

    -- Vendas
    CREATE TABLE sales (
      id INT AUTO_INCREMENT PRIMARY KEY,
      product_id INT NOT NULL,
      user_id INT NOT NULL,
      quantity INT NOT NULL,
      unit_price DECIMAL(10,2) NOT NULL,
      sale_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (product_id) REFERENCES products(id) ON UPDATE CASCADE,
      FOREIGN KEY (user_id)    REFERENCES users(id)    ON UPDATE CASCADE
    );

    -- Popula usuários (senha BCrypt)
    INSERT INTO users (username, password_hash, role) VALUES
      ('admin', '$2a$10$Dow1fUx5Dm5SbR4W/6e9LuqS/heXzc/3mbFDoZx1VPsGdQe7I/3PO', 'ADMIN'),
      ('joao',  '$2a$10$K7pZLBU7JjEUIZ6y/8h8fu0JTIeXJUox.sIwpFGRZz3uEeQvJjVDi', 'EMPLOYEE'),
      ('maria', '$2a$10$Y7M9bInRGNVTn3HyltlH2uyuHkQIgP0xkMCY6yJwLtjn57bVtEJFe', 'EMPLOYEE'),
      ('ana',   '$2a$10$U6tNS8z.Hh3QXZxGJYKfSeKXic2ARssd8NF9l8R.S8sxcnZBJbQS6', 'EMPLOYEE');
    -- (essas hashes correspondem a "admin123", "joao123", "maria123", "ana123")

    -- Popula produtos
    INSERT INTO products (name, price, quantity) VALUES
      ('Notebook Lenovo',    3500.00, 10),
      ('Smartphone Samsung', 2200.00, 15),
      ('Fone JBL',            350.00, 25),
      ('Monitor LG 24"',     1100.00, 12),
      ('Teclado Mecânico',    450.00, 20),
      ('Mouse Gamer',         250.00, 30),
      ('Cadeira Gamer',      1600.00, 8),
      ('HD SSD 1TB',          600.00, 14),
      ('Webcam FullHD',       300.00, 18),
      ('Headset HyperX',      700.00, 10);

    -- Simula vendas nos últimos dias
    INSERT INTO sales (product_id,user_id,quantity,unit_price,sale_date) VALUES
      (1,2,1,3500.00,NOW() - INTERVAL 6 DAY),
      (3,3,2, 350.00,NOW() - INTERVAL 5 DAY),
      (2,4,1,2200.00,NOW() - INTERVAL 4 DAY),
      (5,2,1, 450.00,NOW() - INTERVAL 3 DAY),
      (4,3,1,1100.00,NOW() - INTERVAL 2 DAY),
      (6,4,2, 250.00,NOW() - INTERVAL 1 DAY),
      (1,2,1,3500.00,NOW()),
      (3,3,1, 350.00,NOW()),
      (6,4,2, 250.00,NOW());
    ```

> **Obs.** As senhas de exemplo estão pré-hashadas via BCrypt.

---

## 2. Build com Maven

No diretório raiz do projeto, execute:

```bash
mvn clean package
```

Isso gera um JAR “all-in-one” em:

```
target/loja-1.0-SNAPSHOT-all.jar
```

---

## 3. Executar a aplicação

Basta usar:

```bash
java -jar target/loja-1.0-SNAPSHOT-all.jar
```

- Uma **Splash Screen** (`splash.png`) aparecerá por 2 segundos.
- Em seguida, surgirá a janela de **Login**.

**Credenciais de teste:**

| Usuário | Senha      | Função    |
| ------- | ---------- | --------- |
| admin   | admin   | ADMIN     |
| joao    | joao    | EMPLOYEE  |
| maria   | maria   | EMPLOYEE  |
| ana     | ana     | EMPLOYEE  |

---

## 4. Personalizações

- Configure `src/main/resources/splash.png` para alterar a imagem de splash.
- Ajuste `application.properties` (se desejar externalizar configurações).
- Veja os DAO em `src/main/java/dao` para alterar regras de persistência.

---

Bom trabalho!  
Qualquer dúvida, abra uma _issue_ ou consulte a documentação do projeto.
