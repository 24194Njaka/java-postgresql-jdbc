package com.jdbc.minidishdb.entity;

import com.jdbc.minidishdb.enums.MovementTypeEnum;
import com.jdbc.minidishdb.enums.UnitTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockMovement {
    private int id;
    private StockValue value;
    private MovementTypeEnum type;
    private Instant creationDatetime;
}