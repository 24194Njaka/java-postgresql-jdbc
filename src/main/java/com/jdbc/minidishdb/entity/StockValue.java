package com.jdbc.minidishdb.entity;

import com.jdbc.minidishdb.enums.UnitTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockValue {
    private Double quantity;
    private UnitTypeEnum unit;
}
