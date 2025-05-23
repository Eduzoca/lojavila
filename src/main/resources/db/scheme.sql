-- Criação do banco de dados
DROP IF EXISTS lojavila
CREATE DATABASE lojavila
USE lojavila

-- Limpeza
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- Criação da tabela de usuários
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'EMPLOYEE') NOT NULL
);

-- Criação da tabela de produtos
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0
);

-- Criação da tabela de vendas
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    sale_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Usuários (ADMIN e EMPLOYEEs)
INSERT INTO users (username, password_hash, role) VALUES
('admin', '$2a$10$pSa60tAVrUa4YOEZE87pFOkaKj3dacjqrz8peZo6AMBYxXqiDDifS', 'ADMIN');
('joao', '$2a$10$9sCGi03AgHFfE.Ilb/QdaO.AHqrFtZvvviHUEPob9JSvwFFFelrl2', 'EMPLOYEE');
('maria', '$2a$10$/yvxVKyzirMHV8g6pxb3du2dK/HG6uXhqwD0CRzMKLxT1DFWd.kd2', 'EMPLOYEE');
('ana', '$2a$10$UeIH4YFiWz1zZXqExeiaGOGn2r9nWH8Zw6ehj34NoaJj34kPvH0NG', 'EMPLOYEE');


-- Produtos iniciais
INSERT INTO products (name, price, quantity) VALUES
('Notebook Lenovo',      3500.00, 10),
('Smartphone Samsung',   2200.00, 15),
('Fone JBL',              350.00, 25),
('Monitor LG 24"',       1100.00, 12),
('Teclado Mecânico',      450.00, 20),
('Mouse Gamer',           250.00, 30),
('Cadeira Gamer',        1600.00, 8),
('HD SSD 1TB',            600.00, 14),
('Webcam FullHD',         300.00, 18),
('Headset HyperX',        700.00, 10);

-- Vendas simuladas nos últimos 7 dias
-- Vamos usar funções NOW() - INTERVAL para simular os dias

-- Vendas do dia -6
INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) VALUES
(1, 2, 1, 3500.00, NOW() - INTERVAL 6 DAY),
(3, 3, 2,  350.00, NOW() - INTERVAL 6 DAY);

-- Vendas do dia -5
INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) VALUES
(2, 4, 1, 2200.00, NOW() - INTERVAL 5 DAY),
(5, 2, 1,  450.00, NOW() - INTERVAL 5 DAY);

-- Vendas do dia -4
INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) VALUES
(4, 3, 1, 1100.00, NOW() - INTERVAL 4 DAY),
(6, 4, 2,  250.00, NOW() - INTERVAL 4 DAY);

-- Vendas do dia -3
INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) VALUES
(3, 2, 1,  350.00, NOW() - INTERVAL 3 DAY),
(7, 3, 1, 1600.00, NOW() - INTERVAL 3 DAY);

-- Vendas do dia -2
INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) VALUES
(2, 4, 2, 2200.00, NOW() - INTERVAL 2 DAY),
(9, 2, 1,  300.00, NOW() - INTERVAL 2 DAY);

-- Vendas do dia -1
INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) VALUES
(8, 3, 1,  600.00, NOW() - INTERVAL 1 DAY),
(10, 4, 1,  700.00, NOW() - INTERVAL 1 DAY);

-- Vendas de hoje
INSERT INTO sales (product_id, user_id, quantity, unit_price, sale_date) VALUES
(1, 2, 1, 3500.00, NOW()),
(3, 3, 1,  350.00, NOW()),
(6, 4, 2,  250.00, NOW());