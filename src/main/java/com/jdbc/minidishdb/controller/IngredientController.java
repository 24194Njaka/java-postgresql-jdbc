package com.jdbc.minidishdb.controller;

import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.entity.StockMovement;
import com.jdbc.minidishdb.entity.StockValue;
import com.jdbc.minidishdb.enums.UnitTypeEnum;
import com.jdbc.minidishdb.repository.IngredientRepository;
import com.jdbc.minidishdb.repository.StockMovementRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;

    public IngredientController(IngredientRepository ingredientRepository,
                                StockMovementRepository stockMovementRepository) {
        this.ingredientRepository = ingredientRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    // GET /ingredients
    @GetMapping
    public ResponseEntity<?> findIngredients() {
        try {
            List<Ingredient> ingredients = ingredientRepository.findIngredients(1, 100);
            return ResponseEntity.ok(ingredients);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // GET /ingredients/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> findIngredientById(@PathVariable int id) {
        try {
            Ingredient ingredient = ingredientRepository.findIngredientById(id);
            return ResponseEntity.ok(ingredient);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Ingredient.id=" + id + " is not found");
        }
    }

    // GET /ingredients/{id}/stock?at={temporal}&unit={unit}
    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStockValueAt(
            @PathVariable int id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {
        try {
            if (at == null || unit == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Either mandatory query parameter `at` or `unit` is not provided.");
            }

            Ingredient ingredient = ingredientRepository.findIngredientById(id);
            ingredient.setStockMovementList(
                    stockMovementRepository.findByIngredientId(id));

            UnitTypeEnum unitType = UnitTypeEnum.valueOf(unit.toUpperCase());
            StockValue stockValue = ingredient.getStockValueAt(at);
            stockValue.setUnit(unitType);

            return ResponseEntity.ok(stockValue);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("is not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Ingredient.id=" + id + " is not found");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // GET /ingredients/{id}/stockMovements?from={from}&to={to}
    @GetMapping("/{id}/stockMovements")
    public ResponseEntity<?> findStockMovements(
            @PathVariable int id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        try {
            ingredientRepository.findIngredientById(id);

            List<StockMovement> movements = stockMovementRepository
                    .findByIngredientIdAndDateRange(id, from, to);

            return ResponseEntity.ok(movements);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("is not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Ingredient.id=" + id + " is not found");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    @PostMapping("/{id}/stockMovements")
    public ResponseEntity<?> addStockMovements(
            @PathVariable int id,
            @RequestBody(required = false) List<StockMovement> stockMovements) {

        try {
            if (stockMovements == null || stockMovements.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Request body is mandatory");
            }


            Ingredient ingredient = ingredientRepository.findIngredientById(id);
            ingredient.setStockMovementList(stockMovements);

            Ingredient saved = stockMovementRepository.saveIngredient(ingredient);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(saved.getStockMovementList());


        } catch (RuntimeException e) {
            if (e.getMessage().contains("is not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Ingredient.id=" + id + " is not found");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }
}
