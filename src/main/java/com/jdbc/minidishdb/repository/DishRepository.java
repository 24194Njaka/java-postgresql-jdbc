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
}





