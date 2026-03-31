CREATE DATABASE mini_dish_db;

CREATE USER mini_dish_db_manager WITH PASSWORD 'password';

GRANT ALL PRIVILEGES ON DATABASE mini_dish_db TO mini_dish_db_manager;

\c mini_dish_db;

GRANT ALL ON SCHEMA public TO mini_dish_db_manager;

          GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mini_dish_db_manager;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mini_dish_db_manager;