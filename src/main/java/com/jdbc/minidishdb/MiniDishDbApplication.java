package com.jdbc.minidishdb;

import com.jdbc.minidishdb.entity.Dish;
import com.jdbc.minidishdb.entity.Ingredient;
import com.jdbc.minidishdb.enums.CategoryEnum;
import com.jdbc.minidishdb.enums.DishTypeEnum;
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


    }


    }




