package com.jdbc.minidishdb.repository;

import com.jdbc.minidishdb.entity.Dish;
import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.enums.CategoryEnum;
import com.jdbc.minidishdb.enums.DishTypeEnum;
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

    private Dish mapToDish(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setId(rs.getInt("id"));
        dish.setName(rs.getString("name"));
        dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
        return dish;
    }

    private Ingredient mapToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("ing_id"));
        ingredient.setName(rs.getString("ing_name"));
        ingredient.setPrice(rs.getDouble("ing_price"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ing_category")));
        return ingredient;
    }

    public Dish findDishById(Integer id) {
        String sql = """
                SELECT d.id, d.name, d.dish_type,
                       i.id AS ing_id, i.name AS ing_name,
                       i.price AS ing_price, i.category AS ing_category
                FROM dish d
                LEFT JOIN ingredient i ON i.id_dish = d.id
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
                    dish.getIngredients().add(mapToIngredient(rs));
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

    public Dish saveDish(Dish dishToSave) {
        String checkSql = "SELECT COUNT(*) FROM dish WHERE id = ?";
        String insertSql = "INSERT INTO dish (name, dish_type) VALUES (?, ?) RETURNING id";
        String updateSql = "UPDATE dish SET name = ?, dish_type = ? WHERE id = ?";
        String unlinkSql = "UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?";
        String linkSql   = "UPDATE ingredient SET id_dish = ? WHERE id = ?";

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
                    insertPs.setObject(2, dishToSave.getDishType().name(), Types.OTHER);
                    ResultSet insertRs = insertPs.executeQuery();
                    insertRs.next();
                    dishId = insertRs.getInt("id");
                    dishToSave.setId(dishId);
                } else {
                    PreparedStatement updatePs = connection.prepareStatement(updateSql);
                    updatePs.setString(1, dishToSave.getName());
                    updatePs.setObject(2, dishToSave.getDishType().name(), Types.OTHER);
                    updatePs.setInt(3, dishToSave.getId());
                    updatePs.executeUpdate();
                    dishId = dishToSave.getId();
                }

                PreparedStatement unlinkPs = connection.prepareStatement(unlinkSql);
                unlinkPs.setInt(1, dishId);
                unlinkPs.executeUpdate();

                if (dishToSave.getIngredients() != null) {
                    PreparedStatement linkPs = connection.prepareStatement(linkSql);
                    for (Ingredient ingredient : dishToSave.getIngredients()) {
                        linkPs.setInt(1, dishId);
                        linkPs.setInt(2, ingredient.getId());
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

    public List<Dish> findDishsByIngredientName(String ingredientName) {
        String sql = """
            SELECT d.id, d.name, d.dish_type,
                   i.id AS ing_id, i.name AS ing_name,
                   i.price AS ing_price, i.category AS ing_category
            FROM dish d
            LEFT JOIN ingredient i ON i.id_dish = d.id
            WHERE i.name ILIKE ?
            """;

        List<Dish> dishes = new ArrayList<>();

        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, "%" + ingredientName + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int dishId = rs.getInt("id");

                // Chercher si le plat existe déjà dans la liste
                Dish dish = dishes.stream()
                        .filter(d -> d.getId() == dishId)
                        .findFirst()
                        .orElse(null);

                if (dish == null) {
                    dish = mapToDish(rs);
                    dishes.add(dish);
                }

                if (rs.getInt("ing_id") != 0) {
                    dish.getIngredients().add(mapToIngredient(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findDishsByIngredientName : " + e.getMessage());
        }

        return dishes;
    }
}





