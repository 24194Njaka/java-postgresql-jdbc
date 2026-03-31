package com.jdbc.minidishdb.repository;

import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.enums.CategoryEnum;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Ingredient mapToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setPrice(rs.getDouble("price"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
        return ingredient;
    }

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM ingredient ORDER BY id LIMIT ? OFFSET ?";

        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            int offset = (page - 1) * size;
            ps.setInt(1, size);
            ps.setInt(2, offset);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredients.add(mapToIngredient(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredients : " + e.getMessage());
        }

        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        String checkSql = "SELECT COUNT(*) FROM ingredient WHERE name = ?";
        String insertSql = "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?::ingredient_category) RETURNING id";

        List<Ingredient> created = new ArrayList<>();

        try (Connection connection = dataSource.getDBConnection()) {
            connection.setAutoCommit(false);

            try {
                for (Ingredient ingredient : newIngredients) {

                    // Vérifier si l'ingrédient existe déjà
                    PreparedStatement checkPs = connection.prepareStatement(checkSql);
                    checkPs.setString(1, ingredient.getName());
                    ResultSet rs = checkPs.executeQuery();
                    rs.next();
                    int count = rs.getInt(1);

                    if (count > 0) {
                        connection.rollback();
                        throw new RuntimeException(
                                "Ingredient '" + ingredient.getName() + "' already exists !"
                        );
                    }

                    // Insérer l'ingrédient
                    PreparedStatement insertPs = connection.prepareStatement(insertSql);
                    insertPs.setString(1, ingredient.getName());
                    insertPs.setDouble(2, ingredient.getPrice());
                    insertPs.setString(3, ingredient.getCategory().name());

                    ResultSet insertRs = insertPs.executeQuery();
                    if (insertRs.next()) {
                        ingredient.setId(insertRs.getInt("id"));
                        created.add(ingredient);
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Erreur createIngredients : " + e.getMessage());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion : " + e.getMessage());
        }

        return created;
    }

    public Ingredient findIngredientById(int id) {
        String sql = "SELECT * FROM ingredient WHERE id = ?";
        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapToIngredient(rs);
            } else {
                throw new RuntimeException("Ingredient.id=" + id + " is not found");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredientById : " + e.getMessage());
        }
    }

}






