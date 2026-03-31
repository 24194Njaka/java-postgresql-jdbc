package com.jdbc.minidishdb;

import com.jdbc.minidishdb.entity.*;
import com.jdbc.minidishdb.enums.CategoryEnum;
import com.jdbc.minidishdb.enums.DishTypeEnum;
import com.jdbc.minidishdb.enums.UnitTypeEnum;
import com.jdbc.minidishdb.repository.DataSource;
import com.jdbc.minidishdb.repository.DishRepository;
import com.jdbc.minidishdb.repository.IngredientRepository;
import com.jdbc.minidishdb.repository.StockMovementRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
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




