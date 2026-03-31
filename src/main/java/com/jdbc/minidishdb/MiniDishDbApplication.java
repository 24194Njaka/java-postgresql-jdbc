package com.jdbc.minidishdb;

import com.jdbc.minidishdb.entity.Dish;
import com.jdbc.minidishdb.entity.DishIngredient;
import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.enums.CategoryEnum;
import com.jdbc.minidishdb.enums.DishTypeEnum;
import com.jdbc.minidishdb.enums.UnitTypeEnum;
import com.jdbc.minidishdb.repository.DataSource;
import com.jdbc.minidishdb.repository.DishRepository;
import com.jdbc.minidishdb.repository.IngredientRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class MiniDishDbApplication {

    public static void main(String[] args) {
        DataSource dataSource = new DataSource();
        DishRepository dishRepository = new DishRepository(dataSource);
        IngredientRepository ingredientRepository = new IngredientRepository(dataSource);



        // Test A) findDishById id=1
        System.out.println("=== Test A) findDishById id=1 ===");
        Dish dish = dishRepository.findDishById(1);
        System.out.println("Plat : " + dish.getName());
        dish.getIngredients().forEach(di ->
                System.out.println("  - " + di.getIngredient().getName()
                        + " x" + di.getQuantity() + " " + di.getUnit()));

        // Test getDishCost
        System.out.println("\n=== Test getDishCost ===");
        dishRepository.findAllDishes().forEach(d ->
                System.out.println(d.getName() + " → coût : " + d.getDishCost()));

        // Test getGrossMargin
        System.out.println("\n=== Test getGrossMargin ===");
        dishRepository.findAllDishes().forEach(d -> {
            try {
                System.out.println(d.getName() + " → marge : " + d.getGrossMargin());
            } catch (RuntimeException e) {
                System.out.println(d.getName() + " → Exception : " + e.getMessage());
            }
        });

        // Test K) saveDish nouveau
        System.out.println("\n=== Test K) saveDish nouveau ===");
        Ingredient oignon = new Ingredient();
        oignon.setId(6);
        DishIngredient di = new DishIngredient();
        di.setIngredient(oignon);
        di.setQuantity(1.0);
        di.setUnit(UnitTypeEnum.PCS);
        Dish newDish = new Dish(0, "Soupe de légumes", DishTypeEnum.START,
                new ArrayList<>(List.of(di)), null);
        Dish saved = dishRepository.saveDish(newDish);
        System.out.println("Plat créé : " + saved.getName());
        saved.getIngredients().forEach(d ->
                System.out.println("  - " + d.getIngredient().getName()));
    }
}




