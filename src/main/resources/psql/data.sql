INSERT INTO dish (id, name, dish_type) VALUES
                                           (1, 'Salade fraîche', 'START'),
                                           (2, 'Poulet grillé', 'MAIN'),
                                           (3, 'Riz aux légumes', 'MAIN'),
                                           (4, 'Gâteau au chocolat', 'DESSERT'),
                                           (5, 'Salade de fruits', 'DESSERT');

INSERT INTO ingredient (id, name, price, category, id_dish) VALUES
                                                                (1, 'Laitue',   800.00,  'VEGETABLE', 1),
                                                                (2, 'Tomate',   600.00,  'VEGETABLE', 1),
                                                                (3, 'Poulet',   4500.00, 'ANIMAL',    2),
                                                                (4, 'Chocolat', 3000.00, 'OTHER',     4),
                                                              (5, 'Beurre',   2500.00, 'DAIRY',     4);

INSERT INTO dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit) VALUES
                                                                                      (1, 1, 1, 0.20, 'KG'),
                                                                                      (2, 1, 2, 0.15, 'KG'),
                                                                                      (3, 2, 3, 1.00, 'KG'),
                                                                                      (4, 4, 4, 0.30, 'KG'),
                                                                                      (5, 4, 5, 0.20, 'KG');

-- Mise à jour des prix de vente
UPDATE dish SET selling_price = 3500.00  WHERE id = 1;
UPDATE dish SET selling_price = 12000.00 WHERE id = 2;
UPDATE dish SET selling_price = NULL     WHERE id = 3;
UPDATE dish SET selling_price = 8000.00  WHERE id = 4;
UPDATE dish SET selling_price = NULL     WHERE id = 5;