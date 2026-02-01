package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class DailySalesData {
    ZonedDateTime date;
    private int totalOrders;
    private int totalProducts;
    private double totalRevenue;
    private List<DailyClientSalesData> clients;
}
