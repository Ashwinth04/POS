package com.increff.pos.db.subdocuments;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyClientSalesPojo {
    private String clientName;
    private int totalProducts;
    private double totalRevenue;
}
