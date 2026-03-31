package com.jdbc.minidishdb.controller;

import com.jdbc.minidishdb.entity.Dish;
import com.jdbc.minidishdb.entity.DishIngredient;
import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.enums.UnitTypeEnum;
import com.jdbc.minidishdb.repository.DishRepository;
import com.jdbc.minidishdb.repository.IngredientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/dishes")
public class DishController {

    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    public DishController(DishRepository dishRepository,
                          IngredientRepository ingredientRepository) {
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
    }

    // GET /dishes
    @GetMapping
    public ResponseEntity<?> findAllDishes() {
        try {
            List<Dish> dishes = dishRepository.findAllDishes();
            return ResponseEntity.ok(dishes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // PUT /dishes/{id}/ingredients
    @PutMapping("/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable int id,
            @RequestBody(required = false) List<Ingredient> ingredients) {
        try {
            // Vérifier le body
            if (ingredients == null || ingredients.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Request body is mandatory");
            }

            // Vérifier si le plat existe
            Dish dish = dishRepository.findDishById(id);

            // Construire la liste DishIngredient
            List<DishIngredient> dishIngredients = new ArrayList<>();
            for (Ingredient ingredient : ingredients) {
                // Ignorer les ingrédients qui n'existent pas en BD
                try {
                    ingredientRepository.findIngredientById(ingredient.getId());
                    DishIngredient di = new DishIngredient();
                    di.setIngredient(ingredient);
                    di.setQuantity(1.0);
                    di.setUnit(UnitTypeEnum.KG);
                    dishIngredients.add(di);
                } catch (RuntimeException e) {
                    // Ingrédient non trouvé → on ignore
                }
            }

            dish.setIngredients(dishIngredients);
            Dish updated = dishRepository.saveDish(dish);
            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("is not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Dish.id=" + id + " is not found");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }
}







