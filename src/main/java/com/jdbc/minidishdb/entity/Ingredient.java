package com.jdbc.minidishdb.entity;

import com.jdbc.minidishdb.enums.CategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ingredient {
    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private Dish dish;

    public String getDishName() {
        if (dish == null) return null;
        return dish.getName();
    }
}
