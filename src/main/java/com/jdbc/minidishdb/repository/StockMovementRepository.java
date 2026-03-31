package com.jdbc.minidishdb.repository;

import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.entity.StockMovement;
import com.jdbc.minidishdb.entity.StockValue;
import com.jdbc.minidishdb.enums.MovementTypeEnum;
import com.jdbc.minidishdb.enums.UnitTypeEnum;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class StockMovementRepository {

    private final DataSource dataSource;

    public StockMovementRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Mapper StockMovement
    private StockMovement mapToStockMovement(ResultSet rs) throws SQLException {
        StockMovement sm = new StockMovement();
        sm.setId(rs.getInt("id"));
        sm.setType(MovementTypeEnum.valueOf(rs.getString("type")));
        sm.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());
        StockValue value = new StockValue();
        value.setQuantity(rs.getDouble("quantity"));
        value.setUnit(UnitTypeEnum.valueOf(rs.getString("unit")));
        sm.setValue(value);
        return sm;
    }

    // Trouver les mouvements par ingrédient
    public List<StockMovement> findByIngredientId(int ingredientId) {
        String sql = """
                SELECT * FROM stock_movement
                WHERE id_ingredient = ?
                ORDER BY creation_datetime ASC
                """;

        List<StockMovement> movements = new ArrayList<>();

        try (Connection connection = dataSource.getDBConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, ingredientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                movements.add(mapToStockMovement(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur findByIngredientId : " + e.getMessage());
        }

        return movements;
    }

    // Sauvegarder un ingrédient avec ses mouvements
    public Ingredient saveIngredient(Ingredient toSave) {
        String insertIngSql = """
                INSERT INTO ingredient (name, price, category)
                VALUES (?, ?, ?::ingredient_category)
                ON CONFLICT (id) DO UPDATE
                SET name = ?, price = ?, category = ?::ingredient_category
                RETURNING id
                """;
        String insertMovSql = """
                INSERT INTO stock_movement (id_ingredient, quantity, type, unit, creation_datetime)
                VALUES (?, ?, ?::mouvement_type, ?::unit_type, ?)
                ON CONFLICT (id) DO NOTHING
                """;

        try (Connection connection = dataSource.getDBConnection()) {
            connection.setAutoCommit(false);

            try {
                // Sauvegarder l'ingrédient
                PreparedStatement ingPs = connection.prepareStatement(insertIngSql);
                ingPs.setString(1, toSave.getName());
                ingPs.setDouble(2, toSave.getPrice());
                ingPs.setString(3, toSave.getCategory().name());
                ingPs.setString(4, toSave.getName());
                ingPs.setDouble(5, toSave.getPrice());
                ingPs.setString(6, toSave.getCategory().name());
                ResultSet rs = ingPs.executeQuery();
                if (rs.next()) {
                    toSave.setId(rs.getInt("id"));
                }

                // Sauvegarder les mouvements
                if (toSave.getStockMovementList() != null) {
                    PreparedStatement movPs = connection.prepareStatement(insertMovSql);
                    for (StockMovement sm : toSave.getStockMovementList()) {
                        movPs.setInt(1, toSave.getId());
                        movPs.setDouble(2, sm.getValue().getQuantity());
                        movPs.setString(3, sm.getType().name());
                        movPs.setString(4, sm.getValue().getUnit().name());
                        movPs.setTimestamp(5, Timestamp.from(sm.getCreationDatetime()));
                        movPs.executeUpdate();
                    }
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Erreur saveIngredient : " + e.getMessage());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion : " + e.getMessage());
        }

        // Recharger les mouvements
        toSave.setStockMovementList(findByIngredientId(toSave.getId()));
        return toSave;
    }
}
