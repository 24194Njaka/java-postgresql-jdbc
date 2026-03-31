package com.jdbc.minidishdb.repository;

import com.jdbc.minidishdb.entity.Dish;
import com.jdbc.minidishdb.entity.DishIngredient;
import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.enums.CategoryEnum;
import com.jdbc.minidishdb.enums.DishTypeEnum;
import com.jdbc.minidishdb.enums.UnitTypeEnum;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class DishRepository {

    private final DataSource dataSource;

    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Mapper Dish
    private Dish mapToDish(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        double sellingPrice = rs.getDouble("selling_price");
        dish.setSellingPrice(rs.wasNull() ? null : sellingPrice);
        return dish;
    }

    // Mapper DishIngredient
    private DishIngredient mapToDishIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("ing_id"));
        ingredient.setName(rs.getString("ing_name"));
        ingredient.setPrice(rs.getDouble("ing_price"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ing_category")));

        DishIngredient di = new DishIngredient();
        di.setIngredient(ingredient);
        di.setQuantity(rs.getDouble("quantity_required"));
        di.setUnit(UnitTypeEnum.valueOf(rs.getString("unit")));
        return di;
    }

    // A) findDishById
    public Dish findDishById(Integer id) {
        String sql = """
                SELECT d.id, d.name, d.dish_type, d.selling_price,
                       i.id AS ing_id, i.name AS ing_name,
                       i.price AS ing_price, i.category AS ing_category,
                       di.quantity_required, di.unit
                FROM dish d
                LEFT JOIN dish_ingredient di ON di.id_dish = d.id
                LEFT JOIN ingredient i ON di.id_ingredient = i.id
                WHERE d.id = ?
                """;

        Dish dish = null;

        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (dish == null) {
                    dish = mapToDish(rs);
                }
                if (rs.getInt("ing_id") != 0) {
                    dish.getIngredients().add(mapToDishIngredient(rs));
                }
            }

            if (dish == null) {
                throw new RuntimeException("Dish.id=" + id + " is not found");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findDishById : " + e.getMessage());
        }

        return dish;
    }

    // D) saveDish
    public Dish saveDish(Dish dishToSave) {
        String checkSql    = "SELECT COUNT(*) FROM dish WHERE id = ?";
        String insertSql   = "INSERT INTO dish (name, dish_type) VALUES (?, ?::dish_type) RETURNING id";
        String updateSql   = "UPDATE dish SET name = ?, dish_type = ?::dish_type WHERE id = ?";
        String unlinkSql   = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        String linkSql     = "INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit) VALUES (?, ?, ?, ?::unit_type)";

        try (Connection connection = dataSource.getDBConnection()) {
            connection.setAutoCommit(false);

            try {
                int dishId;

                PreparedStatement checkPs = connection.prepareStatement(checkSql);
                checkPs.setInt(1, dishToSave.getId());
                ResultSet rs = checkPs.executeQuery();
                rs.next();
                boolean exists = rs.getInt(1) > 0;

                if (!exists) {
                    PreparedStatement insertPs = connection.prepareStatement(insertSql);
                    insertPs.setString(1, dishToSave.getName());
                    insertPs.setString(2, dishToSave.getDishType().name());
                    ResultSet insertRs = insertPs.executeQuery();
                    insertRs.next();
                    dishId = insertRs.getInt("id");
                    dishToSave.setId(dishId);
                } else {
                    PreparedStatement updatePs = connection.prepareStatement(updateSql);
                    updatePs.setString(1, dishToSave.getName());
                    updatePs.setString(2, dishToSave.getDishType().name());
                    updatePs.setInt(3, dishToSave.getId());
                    updatePs.executeUpdate();
                    dishId = dishToSave.getId();
                }

                // Dissocier anciens ingrédients
                PreparedStatement unlinkPs = connection.prepareStatement(unlinkSql);
                unlinkPs.setInt(1, dishId);
                unlinkPs.executeUpdate();

                // Associer nouveaux ingrédients
                if (dishToSave.getIngredients() != null) {
                    PreparedStatement linkPs = connection.prepareStatement(linkSql);
                    for (DishIngredient di : dishToSave.getIngredients()) {
                        linkPs.setInt(1, dishId);
                        linkPs.setInt(2, di.getIngredient().getId());
                        linkPs.setDouble(3, di.getQuantity());
                        linkPs.setString(4, di.getUnit().name());
                        linkPs.executeUpdate();
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Erreur saveDish : " + e.getMessage());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion : " + e.getMessage());
        }

        return findDishById(dishToSave.getId());
    }

    // E) findDishsByIngredientName
    public List<Dish> findDishsByIngredientName(String ingredientName) {
        String sql = """
                SELECT d.id, d.name, d.dish_type, d.selling_price,
                       i.id AS ing_id, i.name AS ing_name,
                       i.price AS ing_price, i.category AS ing_category,
                       di.quantity_required, di.unit
                FROM dish d
                LEFT JOIN dish_ingredient di ON di.id_dish = d.id
                LEFT JOIN ingredient i ON di.id_ingredient = i.id
                WHERE i.name ILIKE ?
                """;

        List<Dish> dishes = new ArrayList<>();

        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int dishId = rs.getInt("id");
                Dish dish = dishes.stream()
                        .filter(d -> d.getId() == dishId)
                        .findFirst()
                        .orElse(null);

                if (dish == null) {
                    dish = mapToDish(rs);
                    dishes.add(dish);
                }

                if (rs.getInt("ing_id") != 0) {
                    dish.getIngredients().add(mapToDishIngredient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findDishsByIngredientName : " + e.getMessage());
        }

        return dishes;
    }

    // Trouver tous les plats
    public List<Dish> findAllDishes() {
        String sql = """
                SELECT d.id, d.name, d.dish_type, d.selling_price,
                       i.id AS ing_id, i.name AS ing_name,
                       i.price AS ing_price, i.category AS ing_category,
                       di.quantity_required, di.unit
                FROM dish d
                LEFT JOIN dish_ingredient di ON di.id_dish = d.id
                LEFT JOIN ingredient i ON di.id_ingredient = i.id
                ORDER BY d.id
                """;

        List<Dish> dishes = new ArrayList<>();

        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int dishId = rs.getInt("id");
                Dish dish = dishes.stream()
                        .filter(d -> d.getId() == dishId)
                        .findFirst()
                        .orElse(null);

                if (dish == null) {
                    dish = mapToDish(rs);
                    dishes.add(dish);
                }

                if (rs.getInt("ing_id") != 0) {
                    dish.getIngredients().add(mapToDishIngredient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findAllDishes : " + e.getMessage());
        }

        return dishes;
    }
}

