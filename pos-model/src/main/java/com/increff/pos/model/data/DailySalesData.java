package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DailySalesData {
    private int totalOrders;
    private int totalProducts;
    private double totalRevenue;
    private List<DailyClientSalesData> clients;
}
