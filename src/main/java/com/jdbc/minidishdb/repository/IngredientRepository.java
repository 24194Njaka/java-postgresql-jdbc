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
        String insertSql = "INSERT INTO ingredient (name, price, category) VALUES (?, ?, ?) RETURNING id";

        List<Ingredient> created = new ArrayList<>();

        try (Connection connection = dataSource.getDBConnection()) {

            // Désactiver l'auto-commit pour gérer l'atomicité
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
                        // Annuler toute l'opération
                        connection.rollback();
                        throw new RuntimeException(
                                "Ingredient '" + ingredient.getName() + "' already exists !"
                        );
                    }

                    PreparedStatement insertPs = connection.prepareStatement(insertSql);
                    insertPs.setString(1, ingredient.getName());
                    insertPs.setDouble(2, ingredient.getPrice());
                    insertPs.setObject(3, ingredient.getCategory().name(),
                            Types.OTHER);

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

    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size) {

        List<Ingredient> ingredients = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT i.id, i.name, i.price, i.category
            FROM ingredient i
            LEFT JOIN dish d ON i.id_dish = d.id
            WHERE 1=1
            """);

        // Filtre nom ingrédient
        if (ingredientName != null && !ingredientName.isBlank()) {
            sql.append(" AND i.name ILIKE ? ");
            params.add("%" + ingredientName + "%");
        }

        // Filtre catégorie
        if (category != null) {
            sql.append(" AND i.category = ?::ingredient_category ");
            params.add(category.name());
        }

        // Filtre nom du plat
        if (dishName != null && !dishName.isBlank()) {
            sql.append(" AND d.name ILIKE ? ");
            params.add("%" + dishName + "%");
        }

        // Pagination
        sql.append(" ORDER BY i.id LIMIT ? OFFSET ? ");
        params.add(size);
        params.add((page - 1) * size);

        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            // Injecter les paramètres dynamiquement
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredients.add(mapToIngredient(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findIngredientsByCriteria : " + e.getMessage());
        }

        return ingredients;
    }
}





