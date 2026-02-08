package com.increff.pos.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class DailyClientSalesPojo {
    private String clientName;
    private int totalProducts;
    private double totalRevenue;
}
