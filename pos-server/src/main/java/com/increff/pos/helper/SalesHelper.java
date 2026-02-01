package com.increff.pos.helper;

import com.increff.pos.db.SalesPojo;
import com.increff.pos.model.data.DailySalesData;

public class SalesHelper {
    public static DailySalesData convertToDto(SalesPojo salesPojo) {

        DailySalesData data = new DailySalesData();

        data.setDate(salesPojo.getDate());
        data.setTotalOrders(salesPojo.getTotalOrders());
        data.setTotalProducts(salesPojo.getTotalProducts());
        data.setTotalRevenue(salesPojo.getTotalRevenue());
        data.setClients(salesPojo.getClients());

        return data;
    }
}
