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

CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');

-- Suppression de la colonne id_dish de ingredient
ALTER TABLE ingredient DROP COLUMN id_dish;

-- Ajout du prix de vente à dish (optionnel)
ALTER TABLE dish ADD COLUMN selling_price NUMERIC;

-- Création de la table de jointure DishIngredient
CREATE TABLE dish_ingredient (
                                 id                 SERIAL PRIMARY KEY,
                                 id_dish            INT NOT NULL REFERENCES dish(id),
                                 id_ingredient      INT NOT NULL REFERENCES ingredient(id),
                                 quantity_required  NUMERIC NOT NULL,
                                 unit               unit_type NOT NULL
);