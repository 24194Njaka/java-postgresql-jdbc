package com.jdbc.minidishdb.entity;

import com.jdbc.minidishdb.enums.MovementTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StockMovement {
    private Integer id;
    private StockValue value;
    private MovementTypeEnum type;
    private String creationDatetime;
}