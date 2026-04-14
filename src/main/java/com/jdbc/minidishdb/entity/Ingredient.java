package com.jdbc.minidishdb.entity;

import com.jdbc.minidishdb.enums.CategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ingredient {
    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private List<StockMovement> stockMovementList = new ArrayList<>();

    public StockValue getStockValueAt(String t) {
        double quantity = stockMovementList.stream()
                .filter(sm -> sm.getCreationDatetime().compareTo(t) <= 0)
                .mapToDouble(sm -> {
                    switch (sm.getType()) {
                        case IN:  return sm.getValue().getQuantity();
                        case OUT: return -sm.getValue().getQuantity();
                        default:  return 0.0;
                    }
                })
                .sum();

        return new StockValue(quantity, stockMovementList.isEmpty() ? null
                : stockMovementList.get(0).getValue().getUnit());
    }

}