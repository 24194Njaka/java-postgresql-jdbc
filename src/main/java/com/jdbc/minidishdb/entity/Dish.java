package com.jdbc.minidishdb.entity;

import com.jdbc.minidishdb.enums.DishTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<DishIngredient> ingredients = new ArrayList<>();
    private Double sellingPrice;

    public Double getDishCost() {
        if (ingredients == null || ingredients.isEmpty()) return 0.0;
        return ingredients.stream()
                .mapToDouble(di -> di.getIngredient().getPrice() * di.getQuantity())
                .sum();
    }

    public Double getGrossMargin() {
        if (sellingPrice == null) {
            throw new RuntimeException("Selling price is NULL for dish : " + name);
        }
        return sellingPrice - getDishCost();
    }
}
