\c mini_dish_db;

CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');
CREATE TYPE ingredient_category AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

CREATE TABLE dish (
                      id    SERIAL PRIMARY KEY,
                      name  VARCHAR(255) NOT NULL,
                      dish_type dish_type NOT NULL
);

CREATE TABLE ingredient (
                            id       SERIAL PRIMARY KEY,
                            name     VARCHAR(255) NOT NULL,
                            price    NUMERIC      NOT NULL,
                            category ingredient_category NOT NULL,
                            id_dish  INT REFERENCES dish(id)
);